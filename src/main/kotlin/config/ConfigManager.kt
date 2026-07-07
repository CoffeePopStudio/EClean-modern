package top.e404.eclean.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.SerializationStrategy
import org.bukkit.command.CommandSender
import top.e404.eclean.PL
import top.e404.eclean.config.model.ChunkDensityConfig
import top.e404.eclean.config.model.CleanupConfig
import top.e404.eclean.config.model.DropConfig
import top.e404.eclean.config.model.GlobalConfig
import top.e404.eclean.config.model.LivingConfig
import top.e404.eclean.config.model.TrashcanConfig
import java.io.File

object ConfigManager {
    private val loader = ConfigLoader()
    private val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))

    @Volatile
    private var snapshot = ConfigBundle()

    val current: ConfigBundle
        get() = snapshot

    fun loadAll(sender: CommandSender? = null) {
        snapshot = loadCandidate()
        ConfigRuntimeApplier.apply(snapshot)
        PL.debug { "已加载 modern 配置快照" }
    }

    fun reloadAll(sender: CommandSender? = null) {
        val previous = snapshot
        val candidate = runCatching { loadCandidate() }.getOrElse { error ->
            snapshot = previous
            throw IllegalStateException("重载配置失败: ${error.message}", error)
        }
        snapshot = candidate
        ConfigRuntimeApplier.apply(candidate)
        PL.debug { "已热重载 modern 配置快照" }
    }

    fun replaceSnapshotForTest(bundle: ConfigBundle) {
        snapshot = bundle
    }

    fun update(transform: (ConfigBundle) -> ConfigBundle) {
        snapshot = transform(snapshot)
    }

    fun reloadFromTextForTest(
        globalText: String = "",
        cleanupText: String = "",
        dropText: String = "",
        livingText: String = "",
        chunkDensityText: String = "",
        trashcanText: String = "",
    ) {
        val previous = snapshot
        val candidate = runCatching {
            loader.loadFromText(
                globalText = if (globalText.isBlank()) encode(previous.global, GlobalConfig.serializer()) else globalText,
                cleanupText = if (cleanupText.isBlank()) encode(previous.cleanup, CleanupConfig.serializer()) else cleanupText,
                dropText = if (dropText.isBlank()) encode(previous.drop, DropConfig.serializer()) else dropText,
                livingText = if (livingText.isBlank()) encode(previous.living, LivingConfig.serializer()) else livingText,
                chunkDensityText = if (chunkDensityText.isBlank()) encode(previous.chunkDensity, ChunkDensityConfig.serializer()) else chunkDensityText,
                trashcanText = if (trashcanText.isBlank()) encode(previous.trashcan, TrashcanConfig.serializer()) else trashcanText,
            )
        }.getOrElse {
            snapshot = previous
            throw it
        }
        snapshot = candidate
    }

    private fun loadCandidate(): ConfigBundle {
        maybeBackupLegacyConfig()
        loader.ensureDefaults()
        return loader.loadAll()
    }

    private fun maybeBackupLegacyConfig() {
        val globalFile = File(PL.dataFolder, ConfigFiles.GLOBAL.diskName)
        if (!globalFile.exists()) return
        val otherFilesMissing = ConfigFiles.entries
            .filterNot { it == ConfigFiles.GLOBAL }
            .all { !File(PL.dataFolder, it.diskName).exists() }
        if (!otherFilesMissing) return

        val text = globalFile.readText(Charsets.UTF_8)
        val legacyMarkers = listOf("duration:", "living:", "drop:", "chunk:", "trashcan:", "no_online:")
        if (legacyMarkers.none { text.contains(it) }) return

        val backup = File(PL.dataFolder, "config.legacy.yml")
        if (!backup.exists()) globalFile.copyTo(backup, overwrite = false)
        globalFile.delete()
        PL.warn("检测到旧版单文件配置, 已备份为 config.legacy.yml，请手动迁移到新的多文件配置")
    }

    private fun <T> encode(value: T, serializer: SerializationStrategy<T>): String {
        return yaml.encodeToString(serializer, value)
    }
}
