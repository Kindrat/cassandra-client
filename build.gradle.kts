import com.avast.gradle.dockercompose.tasks.*
import com.github.kindrat.cassandra.CassandraSetupExtension
import com.github.kindrat.cassandra.CassandraSetupPlugin
import com.github.kindrat.cassandra.CassandraSetupTask
import com.github.kindrat.cassandra.Network.PUBLIC_HOST
import de.undercouch.gradle.tasks.download.Download
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.unbrokendome.gradle.plugins.testsets.dsl.testSets

plugins {
    base
    application
    jacoco

    val kotlinVersion = "1.4.10"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion

    id("com.palantir.git-version") version "0.12.3"
    id("pl.allegro.tech.build.axion-release") version "1.12.0"
    id("de.undercouch.download") version "4.1.1"
    id("com.avast.gradle.docker-compose") version "0.13.3"
    id("com.palantir.graal") version "0.7.1-20-g113a84d"
    id("com.google.osdetector") version "1.6.2"
    id("com.github.ben-manes.versions") version "0.33.0"
    id("org.springframework.boot") version "2.3.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("org.unbroken-dome.test-sets") version "3.0.1"
}

apply<CassandraSetupPlugin>()

scmVersion {
    versionIncrementer("incrementMinorIfNotOnRelease", mapOf("releaseBranchPattern" to "release-.*"))
}

testSets {
    createTestSet("functionalTest") {}
}

val gitVersionDetails = scmVersion.version
val dockerComposeBinaryName: String = when (osdetector.os) {
    "osx" -> "docker-compose-Darwin-x86_64"
    "windows" -> "docker-compose-Windows-x86_64.exe"
    "linux" -> "docker-compose-Linux-x86_64"
    else -> throw GradleException("Unsupported os ${osdetector.os}")
}
val isOsLinux: Boolean = osdetector.os == "linux"
val dockerComposeVersion: String by extra
val dockerComposeCassandraPort: String by extra
val dockerComposeCacheDir = "$rootDir/.gradle/docker-compose/"
val dockerComposeCommand = "$dockerComposeCacheDir/docker-compose"
val dockerfileProperties = mapOf(
        "version" to project.version,
        "cassandraPort" to dockerComposeCassandraPort
)

project.version = scmVersion.version

repositories {
    jcenter()
}

dockerCompose {
    dockerComposeWorkingDirectory = "$buildDir/docker-compose"
    captureContainersOutput = true
    useComposeFiles = listOf("docker-compose.yml")
    projectName = "cassandra"
    waitForTcpPorts = false
    removeVolumes = true
    removeOrphans = true
    removeContainers = true
    executable = dockerComposeCommand
}

configure<CassandraSetupExtension> {
    migrationsDir.set(rootDir.resolve("migrations"))
    keyspace.set("test")
    cassandraEndpoint.set("${PUBLIC_HOST.value}:$dockerComposeCassandraPort")
}

javafx {
    version = "16-ea+2"
    modules = listOf("javafx.controls", "javafx.fxml")
    configuration = "implementation"
}

graal {
    mainClass("com.github.kindrat.cassandra.client.CassandraClientGUIKt")
    outputName("cassandra-client")
    javaVersion("11")
    option("-H:+ReportExceptionStackTraces")
    option("--no-fallback")
    option("--report-unsupported-elements-at-runtime")
    option("--allow-incomplete-classpath")
}

springBoot {
    mainClassName = "com.github.kindrat.cassandra.client.Java11Starter"
}

dependencyManagement {
    imports {
        mavenBom(SpringBootPlugin.BOM_COORDINATES)
        mavenBom("io.projectreactor:reactor-bom:Dysprosium-SR12")
    }
}

val functionalTestImplementation: Configuration = configurations["functionalTestImplementation"]

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("io.github.microutils:kotlin-logging:2.0.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.hibernate:hibernate-core")
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("io.projectreactor:reactor-core")
    implementation("io.projectreactor.addons:reactor-extra")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.springframework.data:spring-data-cassandra")
    runtimeOnly("org.codehaus.janino:janino")

    testImplementation("org.springframework.boot:spring-boot-starter-data-cassandra")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core")
}


tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    val downloadDockerCompose by register("downloadDockerCompose", Download::class.java) {
        group = "build setup"

        overwrite(false)
        src("https://github.com/docker/compose/releases/download/$dockerComposeVersion/$dockerComposeBinaryName")
        dest(dockerComposeCommand)

        doFirst { delete(dockerComposeCacheDir) }
        doLast { file(dockerComposeCommand).setExecutable(true) }
    }

    val prepareDockerFiles by register("prepareDockerFiles", Sync::class.java) {
        group = "docker"
        dependsOn(downloadDockerCompose)
        from("docker-compose") {
            filter<ReplaceTokens>("tokens" to dockerfileProperties)
        }
        into("$buildDir/docker-compose")
    }

    withType<ComposePull> {
        dependsOn(prepareDockerFiles)
    }
    withType<ComposeUp> {
        dependsOn(prepareDockerFiles)
    }
    withType<ComposeDown> {
        dependsOn(prepareDockerFiles)
    }
    withType<ComposeDownForced> {
        dependsOn(prepareDockerFiles)
    }
    withType<ComposeBuild> {
        dependsOn(prepareDockerFiles)
    }
    withType<ComposeLogs> {
        dependsOn(prepareDockerFiles)
    }

    val migrateTestDB by register("migrateTestDB", CassandraSetupTask::class.java) {
        dependsOn("composeUp")
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
        }
        val systemProperties = systemProperties
                .minus("java.endorsed.dirs")
                .plus(Pair("user.dir", workingDir))
        systemProperties(systemProperties)

        //Mac OS default TMPDIR env variable points to folder which is not mounted to Docker VM,
        //this fixes mounting temporary files/folders to containers
        if (osdetector.os == "osx") {
            systemProperty("java.io.tmpdir", "/tmp")
        }
    }

    "functionalTest" {
        dependsOn(migrateTestDB)
    }

    wrapper {
        gradleVersion = "6.6.1"
        distributionType = DistributionType.ALL
    }

    jar {
        enabled = true
        manifest {
            attributes(
                    "Main-Class" to "com.github.kindrat.cassandra.client.Java11Starter"
            )
        }
    }

    bootJar {
        mainClassName = "com.github.kindrat.cassandra.client.Java11Starter"
        archiveClassifier.set("exec")
    }

    bootRun {
        this.systemProperties = getSystemProperties()
        main = "com.github.kindrat.cassandra.client.Java11Starter"
        this.systemProperties.remove("java.endorsed.dirs")
    }

    withType<JavaExec> {
        mainClass.set("com.github.kindrat.cassandra.client.Java11Starter")
    }

    withType<JacocoReport> {
        dependsOn(test)
        executionData(fileTree(project.rootDir).include("**/build/jacoco/*.exec"))
        sourceSets(project.sourceSets.main.get())
        reports {
            xml.isEnabled = true
            xml.destination = file("${buildDir}/reports/jacoco/report.xml")
            html.isEnabled = true
            csv.isEnabled = true
        }
    }

    test {
        finalizedBy("composeDown")
    }
}

fun getSystemProperties(): Map<String, Any> {
    return System.getProperties()
            .map { it.key.toString() to it.value }
            .toMap()
}