plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    // MiniMessage / Adventure are general-purpose libraries, not loader APIs.
    api("net.kyori:adventure-api:4.26.1")
    api("net.kyori:adventure-text-minimessage:4.26.1")
    api("net.kyori:adventure-text-serializer-plain:4.26.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
    implementation("com.charleskorn.kaml:kaml:0.104.0")
    implementation("com.cronutils:cron-utils:9.2.1")

    testImplementation(kotlin("test"))
}
