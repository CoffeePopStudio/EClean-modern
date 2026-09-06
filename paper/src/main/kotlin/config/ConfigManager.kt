package top.e404.eclean.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.SerializationStrategy
import org.bukkit.command.CommandSender
import top.e404.eclean.PL
import top.e404.eclean.config.model.ChunkDensityConfig
import top.e404.eclean.config.model.CleanupConfig
import top.e404.eclean.config.model.ConfigProfile
import top.e404.eclean.config.model.DropConfig
import top.e404.eclean.config.model.GlobalConfig
import top.e404.eclean.config.model.LivingConfig
import top.e404.eclean.config.model.PerWorldConfig
import top.e404.eclean.config.model.TrashcanConfig
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ConfigManager {
    private val loader = ConfigLoader()
    private val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))

    @Volatile
    private var snapshot = ConfigBundle()

    @Volatile
    var currentProfile: ConfigProfile = ConfigProfile.NORMAL
        private set

    val current: ConfigBundle
        get() = snapshot

    fun loadAll(sender: CommandSender? = null) {
        maybeBackupLegacyConfig()
        currentProfile = loader.readProfile()
        loader.ensureDefaults(currentProfile)
        snapshot = loader.loadAll(currentProfile)
        ConfigRuntimeApplier.apply(snapshot)
        PL.services.messages.debug { "Config profile ${currentProfile.id} loaded" }
    }

    fun reloadAll(sender: CommandSender? = null) {
        val previous = snapshot
        val previousProfile = currentProfile
        val candidate = runCatching {
            maybeBackupLegacyConfig()
            val profile = loader.readProfile()
            loader.ensureDefaults(profile)
            loader.loadAll(profile) to profile
        }.getOrElse { error ->
            snapshot = previous
            currentProfile = previousProfile
            throw IllegalStateException("Config reload failed: ${error.message}", error)
        }
        currentProfile = candidate.second
        snapshot = candidate.first
        ConfigRuntimeApplier.apply(snapshot)
        PL.services.messages.debug { "Config profile ${currentProfile.id} reloaded" }
    }

    fun switchProfile(profile: ConfigProfile): ConfigProfile {
        val previous = snapshot
        val previousProfile = currentProfile
        return try {
            loader.ensureDefaults(ConfigProfile.NORMAL)
            loader.ensureDefaults(profile)
            loader.writeProfile(profile)
            currentProfile = profile
            snapshot = loader.loadAll(profile)
            ConfigRuntimeApplier.apply(snapshot)
            profile
        } catch (e: Exception) {
            snapshot = previous
            currentProfile = previousProfile
            throw IllegalStateException("Config switch failed: ${e.message}", e)
        }
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
        perWorldText: String = "",
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
                perWorldText = if (perWorldText.isBlank()) encode(previous.perWorld, PerWorldConfig.serializer()) else perWorldText,
            )
        }.getOrElse {
            snapshot = previous
            throw it
        }
        snapshot = candidate
    }

    private fun maybeBackupLegacyConfig() {
        val dataFolder = PL.dataFolder
        val hasLegacy = ConfigFiles.LEGACY_FILES
            .filter { it != "config.yml" }
            .any { File(dataFolder, it).exists() }
        if (!hasLegacy) return

        val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        val backupDir = File(dataFolder, "config-backup-$stamp")
        backupDir.mkdirs()

        ConfigFiles.LEGACY_FILES.forEach { name ->
            val file = File(dataFolder, name)
            if (file.exists()) {
                file.copyTo(File(backupDir, name), overwrite = false)
                file.delete()
            }
        }
        PL.services.messages.warn("检测到旧版根目录配置，已备份到 ${backupDir.name}，并生成新的 normal/dev 配置")
    }

    private fun <T> encode(value: T, serializer: SerializationStrategy<T>): String {
        return yaml.encodeToString(serializer, value)
    }
}