plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin {
    jvmToolchain(25)
}

val paperApi: String = "io.papermc.paper:paper-api:26.1.2.build.+"

val gitCommitHash: String = try {
    ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .directory(rootProject.projectDir)
        .redirectErrorStream(true)
        .start()
        .inputStream.bufferedReader().use { it.readText().trim().ifEmpty { "unknown" } }
} catch (_: Exception) {
    "unknown"
}

dependencies {
    implementation(project(":common"))

    implementation("com.charleskorn.kaml:kaml:0.104.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
    implementation("com.cronutils:cron-utils:9.2.1")

    // paper / folia-compatible api surface
    compileOnly(paperApi)
    // placeholderAPI
    compileOnly("me.clip:placeholderapi:2.12.3")
    // bstats
    implementation("org.bstats:bstats-bukkit:3.2.1")

    // mock bukkit
    testImplementation(kotlin("test"))
    testImplementation(paperApi)
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.115.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.18")
}

tasks {
    shadowJar {
        val archiveName = "EClean-Modern-${gitCommitHash}-${project.version}-paper.jar"
        archiveFileName.set(archiveName)

        // Paper already provides Adventure at runtime.
        exclude("net/kyori/**")
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

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        dependsOn(clean)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val pluginVersion = project.version
        filesMatching("plugin.yml") {
            expand("version" to pluginVersion)
        }
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
