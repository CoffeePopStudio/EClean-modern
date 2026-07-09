package top.e404.eclean.lang

import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import top.e404.eclean.PL
import java.io.File

object MLang {
    private val cache = mutableMapOf<String, String>()

    operator fun get(key: String, vararg placeholder: Pair<String, Any?>): String {
        val raw = cache[key] ?: key
        var result = raw
        for ((k, v) in placeholder) result = result.replace("{$k}", v.toString())
        return result
    }

    fun load(sender: CommandSender? = null) {
        val file = File(PL.dataFolder, "lang.yml")
        if (!file.exists()) PL.saveResource("lang.yml", false)
        val text = file.readText(Charsets.UTF_8)
        val didMigrate = LegacyLangMigrator.migrateIfNeeded(file, text) { readIntoCache(it) }
        if (!didMigrate) {
            readIntoCache(text)
        }
    }

    fun reload(sender: CommandSender? = null) {
        cache.clear()
        load(sender)
    }

    fun put(key: String, value: String) {
        cache[key] = value
    }

    private fun readIntoCache(text: String) {
        cache.clear()
        val yaml = YamlConfiguration()
        yaml.loadFromString(text)
        flatten(yaml, "")
    }

    private fun flatten(section: ConfigurationSection, prefix: String) {
        for (key in section.getKeys(false)) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            val value = section.get(key)
            if (value is ConfigurationSection) {
                flatten(value, fullKey)
            } else {
                cache[fullKey] = value.toString()
            }
        }
    }
}
