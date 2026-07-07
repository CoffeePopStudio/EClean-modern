package top.e404.eclean.config

import org.bukkit.command.CommandSender

object Config {
    val current: ConfigBundle
        get() = ConfigManager.current

    fun load(sender: CommandSender? = null) {
        ConfigManager.loadAll(sender)
    }

    fun reload(sender: CommandSender? = null) {
        ConfigManager.reloadAll(sender)
    }

    fun replaceForTest(bundle: ConfigBundle) {
        ConfigManager.replaceSnapshotForTest(bundle)
    }

    fun update(transform: (ConfigBundle) -> ConfigBundle) {
        ConfigManager.update(transform)
    }
}
