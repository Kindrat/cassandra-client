package com.github.kindrat.cassandra

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class CassandraSetupExtension @Inject constructor(project: Project) {
    val cassandraEndpoint: Property<String> = project.objects.property(String::class.java)
    val keyspace: Property<String> = project.objects.property(String::class.java)
    val migrationsDir: DirectoryProperty = project.objects.directoryProperty()
}