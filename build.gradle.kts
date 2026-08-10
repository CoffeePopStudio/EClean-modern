import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    id("com.gradleup.shadow") version "9.4.3"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "top.e404"
version = "0.2.0"
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
    mavenLocal()
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://nexus.e404.top:3443/repository/maven-snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // paper / folia-compatible api surface
    compileOnly(paper)
    implementation("com.charleskorn.kaml:kaml:0.104.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
    // placeholderAPI
    compileOnly("me.clip:placeholderapi:2.11.6")
    // Bstats
    implementation("org.bstats:bstats-bukkit:3.0.2")

    // mock bukkit
    testImplementation(kotlin("test", "2.4.0"))
    testImplementation(paper)
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.114.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.13")
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
        filesMatching("plugin.yml") {
            expand("version" to project.version)
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
