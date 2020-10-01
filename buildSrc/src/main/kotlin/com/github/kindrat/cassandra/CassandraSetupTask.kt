package com.github.kindrat.cassandra

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

open class CassandraSetupTask : DefaultTask() {
    companion object {
        const val VERSION_KEYSPACE = "version_metadata"
        const val VERSION_TABLE = "version_history"
    }

    init {
        group = "cassandra"
    }

    @TaskAction
    fun setup() {
        val extension = project.extensions.getByType(CassandraSetupExtension::class.java)
        val migrationFiles = extension.migrationsDir.asFileTree.files
        if (migrationFiles.isEmpty()) {
            throw GradleException("No migrations to apply in dir: ${extension.migrationsDir.get().asFile.absolutePath}")
        }
        val endpoint = extension.cassandraEndpoint.get()
        val endpointParts = endpoint.split(":")
        val endpointAddress = InetSocketAddress(endpointParts[0], Integer.parseInt(endpointParts[1]))
        Cluster.builder().addContactPoint { endpointAddress }.build().let {
            if (it.metadata.keyspaces.findLast { s -> s.name!!.contentEquals(VERSION_KEYSPACE) } == null) {
                it.connect("system_schema").let { session: Session? ->
                    session?.execute("CREATE KEYSPACE IF NOT EXISTS $VERSION_KEYSPACE WITH replication = " +
                            "{'class':'SimpleStrategy', 'replication_factor' : 1};")
                }
            }
            val keyspace = it.metadata.getKeyspace(VERSION_KEYSPACE)
            val table = keyspace.getTable(VERSION_TABLE)
            it.connect(VERSION_KEYSPACE).let { session: Session ->
                if (table == null) {
                    session.execute("CREATE table $VERSION_TABLE (version text, name text, time bigint, hash text, PRIMARY KEY (version))")
                }
                val existingMigrations = session.execute("SELECT * FROM $VERSION_TABLE")
                        .filterNotNull()
                        .map { row: Row ->
                            VersionDetails(
                                    row.getString("version"),
                                    row.getString("name"),
                                    row.getLong("time"),
                                    row.getString("hash"),
                                    ""
                            )
                        }
                        .toList()
                        .sortedBy { versionDetails: VersionDetails -> versionDetails.version }

                val fileMigrations = migrationFiles.map { file: File -> parseMigration(file) }
                        .toList()
                        .sortedBy { versionDetails: VersionDetails -> versionDetails.version }

                if (fileMigrations.size < existingMigrations.size) {
                    throw IllegalStateException("DB contains more migrations than provided files")
                }

                existingMigrations.forEachIndexed { index, versionDetails ->
                    val fileVersionDetails = fileMigrations[index]
                    if (!versionDetails.version.contentEquals(fileVersionDetails.version)) {
                        throw IllegalStateException("File $index version ${fileVersionDetails.version} differs from " +
                                "existing migration version ${versionDetails.version}")
                    }
                    if (!versionDetails.name.contentEquals(fileVersionDetails.name)) {
                        throw IllegalStateException("File $index name ${fileVersionDetails.name} differs from " +
                                "existing migration name ${versionDetails.name}")
                    }
                    if (!versionDetails.hash.contentEquals(fileVersionDetails.hash)) {
                        throw IllegalStateException("File $index hash ${fileVersionDetails.hash} differs from " +
                                "existing migration hash ${versionDetails.hash}")
                    }
                }
                fileMigrations.subList(existingMigrations.size, fileMigrations.size).forEach {
                    logger.lifecycle("Applying cassandra ${it.version} ${it.name} migration")
                    session.execute(it.cql)
                    session.execute("INSERT INTO $VERSION_TABLE (version, name, time, hash) VALUES (" +
                            "'${it.version}', '${it.name}', ${System.currentTimeMillis()}, '${it.hash}')")
                }
            }
        }
    }

    private fun parseMigration(file: File): VersionDetails {
        val parts = file.nameWithoutExtension.split("___")
        if (parts.size != 2) {
            throw IllegalArgumentException("Migration filename breaks convention: ${file.nameWithoutExtension}")
        }
        val version = parts[0]
        val name = parts[1]
        val payload = file.readText(StandardCharsets.UTF_8)
        val hash = listOf(version, name, payload).joinToString("-").md5()
        return VersionDetails(version, name, -1, hash, payload)
    }
}

data class VersionDetails(val version: String, val name: String, val time: Long, val hash: String, val cql: String)