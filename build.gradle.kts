import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "top.e404"
version = "0.2.6"
val paper = "io.papermc.paper:paper-api:26.1.2.build.+"

val gitCommitHash: String = try {
    ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .directory(rootProject.projectDir)
        .redirectErrorStream(true)
        .start()
        .inputStream.bufferedReader().use { it.readText().trim().ifEmpty { "unknown" } }
} catch (_: Exception) {
    "unknown"
}

repositories {
    mavenCentral()
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // paper / folia-compatible api surface
    compileOnly(paper)
    implementation("com.charleskorn.kaml:kaml:0.104.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
    // placeholderAPI
    compileOnly("me.clip:placeholderapi:2.12.3")
    // Bstats
    implementation("org.bstats:bstats-bukkit:3.2.1")

    // mock bukkit
    testImplementation(kotlin("test", "2.4.10"))
    testImplementation(paper)
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.115.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.18")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin {
    jvmToolchain(25)
}

tasks {
    shadowJar {
        val archiveName = "EClean-Modern-${gitCommitHash}-${project.version}.jar"
        archiveFileName.set(archiveName)

        relocate("org.bstats", "top.e404.eclean.relocate.bstats")
        relocate("kotlin", "top.e404.eclean.relocate.kotlin")
        relocate("com.charleskorn.kaml", "top.e404.eclean.relocate.kaml")
        exclude("META-INF/**")

        doLast {
            val archiveFile = archiveFile.get().asFile
            println(archiveFile.parentFile.absolutePath)
            println(archiveFile.absolutePath)
        }
    }

    withType<KotlinCompile> {
        dependsOn(clean)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val pluginVersion = project.version
        filesMatching("plugin.yml") {
            expand("version" to pluginVersion)
        }
    }

    test {
        useJUnitPlatform()
        this.systemProperties["eclean.debug"] = true
    }
}

runPaper {
    folia.registerTask()
}

tasks {
    runServer {
        minecraftVersion("26.1.2")
    }
}
