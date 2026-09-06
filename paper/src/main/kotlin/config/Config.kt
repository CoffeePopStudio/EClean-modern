package top.e404.eclean.config

import org.bukkit.command.CommandSender
import top.e404.eclean.config.model.ConfigProfile

object Config {
    val current: ConfigBundle
        get() = ConfigManager.current

    val profile: ConfigProfile
        get() = ConfigManager.currentProfile

    fun load(sender: CommandSender? = null) {
        ConfigManager.loadAll(sender)
    }

    fun reload(sender: CommandSender? = null) {
        ConfigManager.reloadAll(sender)
    }

    fun switchProfile(profile: ConfigProfile): ConfigProfile =
        ConfigManager.switchProfile(profile)

    fun replaceForTest(bundle: ConfigBundle) {
        ConfigManager.replaceSnapshotForTest(bundle)
    }

    fun update(transform: (ConfigBundle) -> ConfigBundle) {
        ConfigManager.update(transform)
    }
}