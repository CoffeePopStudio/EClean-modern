package top.e404.eclean.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.DeserializationStrategy
import top.e404.eclean.PL
import top.e404.eclean.config.model.ChunkDensityConfig
import top.e404.eclean.config.model.CleanupConfig
import top.e404.eclean.config.model.DropConfig
import top.e404.eclean.config.model.GlobalConfig
import top.e404.eclean.config.model.LivingConfig
import top.e404.eclean.config.model.PerWorldConfig
import top.e404.eclean.config.model.TrashcanConfig
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ConfigLoader(
    private val yaml: Yaml = Yaml(configuration = YamlConfiguration(strictMode = false))
) {
    fun ensureDefaults() {
        ConfigFiles.entries.forEach { spec ->
            val target = File(PL.dataFolder, spec.diskName)
            if (target.exists()) return@forEach
            target.parentFile?.mkdirs()
            PL.getResource(spec.resourcePath)?.use { input ->
                Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } ?: error("缺少默认配置资源: ${spec.resourcePath}")
        }
    }

    fun loadAll(): ConfigBundle {
        return ConfigBundle(
            global = read(ConfigFiles.GLOBAL, GlobalConfig.serializer()),
            cleanup = read(ConfigFiles.CLEANUP, CleanupConfig.serializer()),
            drop = read(ConfigFiles.DROP, DropConfig.serializer()),
            living = read(ConfigFiles.LIVING, LivingConfig.serializer()),
            chunkDensity = read(ConfigFiles.CHUNK_DENSITY, ChunkDensityConfig.serializer()),
            trashcan = read(ConfigFiles.TRASHCAN, TrashcanConfig.serializer()),
            perWorld = read(ConfigFiles.PER_WORLD, PerWorldConfig.serializer()),
        )
    }

    fun loadFromText(
        globalText: String,
        cleanupText: String,
        dropText: String,
        livingText: String,
        chunkDensityText: String,
        trashcanText: String,
        perWorldText: String,
    ): ConfigBundle {
        return ConfigBundle(
            global = yaml.decodeFromString(GlobalConfig.serializer(), globalText),
            cleanup = yaml.decodeFromString(CleanupConfig.serializer(), cleanupText),
            drop = yaml.decodeFromString(DropConfig.serializer(), dropText),
            living = yaml.decodeFromString(LivingConfig.serializer(), livingText),
            chunkDensity = yaml.decodeFromString(ChunkDensityConfig.serializer(), chunkDensityText),
            trashcan = yaml.decodeFromString(TrashcanConfig.serializer(), trashcanText),
            perWorld = yaml.decodeFromString(PerWorldConfig.serializer(), perWorldText),
        )
    }

    private fun <T> read(spec: ConfigFiles, serializer: DeserializationStrategy<T>): T {
        val file = File(PL.dataFolder, spec.diskName)
        val text = file.readText(Charsets.UTF_8)
        return yaml.decodeFromString(serializer, text)
    }
}
