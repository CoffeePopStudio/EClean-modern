plugins {
    kotlin("jvm") version "2.4.10" apply false
    kotlin("plugin.serialization") version "2.4.10" apply false
    id("com.gradleup.shadow") version "9.6.1" apply false
    id("xyz.jpenilla.run-paper") version "3.1.0" apply false
}

allprojects {
    group = "top.e404"
    version = "0.2.9"

    repositories {
        mavenCentral()
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        systemProperty("eclean.debug", "true")
    }
}
