package top.e404.eclean.lang

import org.bukkit.command.CommandSender
import top.e404.eclean.PL
import top.e404.eplugin.config.ELangManager

object MLang : ELangManager(PL) {

    override fun load(sender: CommandSender?) {
        saveDefault(sender)
        val text = file.readText(Charsets.UTF_8)
        LegacyLangMigrator.migrateIfNeeded(file, text) {}
        super.load(sender)
    }

    fun reload(sender: CommandSender? = null) {
        load(sender)
    }

    fun put(key: String, value: String) {
        config.set(key, value)
    }
}
