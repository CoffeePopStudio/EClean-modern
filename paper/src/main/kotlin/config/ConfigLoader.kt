package top.e404.eclean.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import top.e404.eclean.PL
import top.e404.eclean.config.model.AdvancedConfig
import top.e404.eclean.config.model.ChunkDensityConfig
import top.e404.eclean.config.model.CleanupConfig
import top.e404.eclean.config.model.ConfigProfile
import top.e404.eclean.config.model.DropConfig
import top.e404.eclean.config.model.GlobalConfig
import top.e404.eclean.config.model.LivingConfig
import top.e404.eclean.config.model.NormalConfig
import top.e404.eclean.config.model.PerWorldConfig
import top.e404.eclean.config.model.ProfileConfig
import top.e404.eclean.config.model.TrashcanConfig
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ConfigLoader(
    private val yaml: Yaml = Yaml(configuration = YamlConfiguration(strictMode = false))
) {
    fun readProfile(): ConfigProfile {
        val file = File(PL.dataFolder, ConfigFiles.PROFILE.diskName)
        if (!file.exists()) ensureRootProfile()
        return try {
            val text = file.readText(Charsets.UTF_8)
            ConfigProfile.fromId(yaml.decodeFromString(ProfileConfig.serializer(), text).profile)
        } catch (e: Exception) {
            PL.logger.warning("config.yml profile 解析失败，使用 normal: ${e.message}")
            ConfigProfile.NORMAL
        }
    }

    fun writeProfile(profile: ConfigProfile) {
        val file = File(PL.dataFolder, ConfigFiles.PROFILE.diskName)
        file.parentFile?.mkdirs()
        val text = yaml.encodeToString(ProfileConfig.serializer(), ProfileConfig(profile.id))
        Files.writeString(file.toPath(), text, Charsets.UTF_8)
    }

    fun ensureDefaults(profile: ConfigProfile) {
        ensureRootProfile()
        ensureNormalDefaults()
        if (profile == ConfigProfile.DEV) {
            ConfigFiles.DEV_ENTRIES.forEach { ensureCopied(it) }
        }
    }

    fun loadAll(profile: ConfigProfile): ConfigBundle = when (profile) {
        ConfigProfile.NORMAL -> loadNormal()
        ConfigProfile.DEV -> loadDev()
    }

    fun loadNormalFromText(text: String): ConfigBundle {
        val normal = try {
            yaml.decodeFromString(NormalConfig.serializer(), text)
        } catch (e: YamlException) {
            PL.logger.warning("config/normal/config.yml 解析失败，使用默认 normal 配置: ${e.message}")
            loadDefaultNormal()
        }
        return normal.toBundle()
    }

    fun loadDevFromText(
        globalText: String,
        cleanupText: String,
        dropText: String,
        livingText: String,
        chunkDensityText: String,
        trashcanText: String,
        perWorldText: String,
        advancedText: String,
    ): ConfigBundle {
        return ConfigBundle(
            global = decodeOrDefault(ConfigFiles.GLOBAL, GlobalConfig.serializer(), globalText),
            cleanup = decodeOrDefault(ConfigFiles.CLEANUP, CleanupConfig.serializer(), cleanupText),
            drop = decodeOrDefault(ConfigFiles.DROP, DropConfig.serializer(), dropText),
            living = decodeOrDefault(ConfigFiles.LIVING, LivingConfig.serializer(), livingText),
            chunkDensity = decodeOrDefault(ConfigFiles.CHUNK_DENSITY, ChunkDensityConfig.serializer(), chunkDensityText),
            trashcan = decodeOrDefault(ConfigFiles.TRASHCAN, TrashcanConfig.serializer(), trashcanText),
            perWorld = decodeOrDefault(ConfigFiles.PER_WORLD, PerWorldConfig.serializer(), perWorldText),
            advanced = decodeOrDefault(ConfigFiles.ADVANCED, AdvancedConfig.serializer(), advancedText),
        )
    }

    /** Kept for tests and legacy callers: loads a dev-style bundle without advanced text. */
    fun loadFromText(
        globalText: String,
        cleanupText: String,
        dropText: String,
        livingText: String,
        chunkDensityText: String,
        trashcanText: String,
        perWorldText: String,
    ): ConfigBundle = loadDevFromText(
        globalText = globalText,
        cleanupText = cleanupText,
        dropText = dropText,
        livingText = livingText,
        chunkDensityText = chunkDensityText,
        trashcanText = trashcanText,
        perWorldText = perWorldText,
        advancedText = "",
    )

    private fun loadNormal(): ConfigBundle {
        val file = File(PL.dataFolder, ConfigFiles.NORMAL.diskName)
        if (!file.exists()) ensureNormalDefaults()
        return loadNormalFromText(file.readText(Charsets.UTF_8))
    }

    private fun loadDev(): ConfigBundle {
        return loadDevFromText(
            globalText = readDev(ConfigFiles.GLOBAL),
            cleanupText = readDev(ConfigFiles.CLEANUP),
            dropText = readDev(ConfigFiles.DROP),
            livingText = readDev(ConfigFiles.LIVING),
            chunkDensityText = readDev(ConfigFiles.CHUNK_DENSITY),
            trashcanText = readDev(ConfigFiles.TRASHCAN),
            perWorldText = readDev(ConfigFiles.PER_WORLD),
            advancedText = readDev(ConfigFiles.ADVANCED),
        )
    }

    private fun readDev(spec: ConfigFiles): String {
        val file = File(PL.dataFolder, spec.diskName)
        if (!file.exists()) {
            ensureCopied(spec)
        }
        return file.readText(Charsets.UTF_8)
    }

    private fun <T> decodeOrDefault(spec: ConfigFiles, serializer: DeserializationStrategy<T>, text: String): T {
        return try {
            yaml.decodeFromString(serializer, text)
        } catch (e: YamlException) {
            PL.logger.warning("配置文件 `${spec.diskName}` 解析失败: ${e.message}，将使用默认配置")
            readDefault(spec, serializer)
        }
    }

    private fun loadDefaultNormal(): NormalConfig {
        val text = PL.getResource(ConfigFiles.NORMAL.resourcePath)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: error("缺少默认配置资源: ${ConfigFiles.NORMAL.resourcePath}")
        return yaml.decodeFromString(NormalConfig.serializer(), text)
    }

    private fun <T> readDefault(spec: ConfigFiles, serializer: DeserializationStrategy<T>): T {
        val defaultText = PL.getResource(spec.resourcePath)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: error("缺少默认配置资源: ${spec.resourcePath}")
        return yaml.decodeFromString(serializer, defaultText)
    }

    private fun ensureRootProfile() {
        val target = File(PL.dataFolder, ConfigFiles.PROFILE.diskName)
        if (target.exists()) return
        copyResource(ConfigFiles.PROFILE, target)
    }

    private fun ensureNormalDefaults() {
        val target = File(PL.dataFolder, ConfigFiles.NORMAL.diskName)
        if (target.exists()) return
        copyResource(ConfigFiles.NORMAL, target)
    }

    private fun ensureCopied(spec: ConfigFiles) {
        val target = File(PL.dataFolder, spec.diskName)
        if (target.exists()) return
        copyResource(spec, target)
    }

    private fun copyResource(spec: ConfigFiles, target: File) {
        target.parentFile?.mkdirs()
        PL.getResource(spec.resourcePath)?.use { input ->
            Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } ?: error("缺少默认配置资源: ${spec.resourcePath}")
    }

    private fun <T> encode(value: T, serializer: SerializationStrategy<T>): String =
        yaml.encodeToString(serializer, value)
}