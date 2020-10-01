package com.github.kindrat.cassandra

import org.gradle.api.Plugin
import org.gradle.api.Project

open class CassandraSetupPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("cassandraSetup", CassandraSetupExtension::class.java, project)
    }
}