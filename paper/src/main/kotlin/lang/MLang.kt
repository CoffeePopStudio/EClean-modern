package top.e404.eclean.lang

import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import top.e404.eclean.PL
import top.e404.eclean.config.Config
import java.io.File

object MLang {
    private val cache = mutableMapOf<String, String>()
    private var currentLanguage = "zh_cn"

    operator fun get(key: String, vararg placeholder: Pair<String, Any?>): String {
        val raw = cache[key] ?: key
        var result = raw
        for ((k, v) in placeholder) result = result.replace("{$k}", v.toString())
        return result
    }

    fun load(sender: CommandSender? = null) {
        currentLanguage = Config.current.global.language.ifBlank { "zh_cn" }
        val langDir = File(PL.dataFolder, "lang")
        langDir.mkdirs()

        listOf("zh_cn", "en_us").forEach { ensureDefaultFile(langDir, it) }

        // 旧版 lang.yml 迁移：如果新的语言文件不存在，则把旧文件复制过去
        val legacy = File(PL.dataFolder, "lang.yml")
        val target = File(langDir, "$currentLanguage.yml")
        if (!target.exists() && legacy.exists()) {
            legacy.copyTo(target, overwrite = false)
        }

        ensureDefaultFile(langDir, currentLanguage)
        val targetText = target.readText(Charsets.UTF_8)
        LegacyLangMigrator.migrateIfNeeded(target, targetText) {}
        mergeMissingKeys(target, currentLanguage)
        readIntoCache(target)
    }

    fun reload(sender: CommandSender? = null) {
        cache.clear()
        load(sender)
    }

    fun put(key: String, value: String) {
        cache[key] = value
    }

    private fun ensureDefaultFile(dir: File, language: String) {
        val target = File(dir, "$language.yml")
        if (!target.exists()) {
            PL.saveResource("lang/$language.yml", false)
        }
    }

    private fun mergeMissingKeys(file: File, language: String) {
        val defaultText = PL.getResource("lang/$language.yml")
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: return
        val defaultMap = flattenToMap(YamlConfiguration().apply { loadFromString(defaultText) })
        val userMap = flattenToMap(YamlConfiguration().apply { loadFromString(file.readText(Charsets.UTF_8)) })
        val missing = defaultMap.filterKeys { it !in userMap }
        if (missing.isEmpty()) return
        appendMissingKeys(file, missing)
    }

    private fun readIntoCache(file: File) {
        cache.clear()
        val yaml = YamlConfiguration()
        try {
            yaml.loadFromString(file.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            PL.logger.warning("lang/${file.name} parse failed, falling back to default: ${e.message}")
            val defaultText = PL.getResource("lang/$currentLanguage.yml")
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                ?: return
            yaml.loadFromString(defaultText)
        }
        cache.putAll(flattenToMap(yaml))
    }

    private fun flattenToMap(section: ConfigurationSection): Map<String, String> {
        val map = mutableMapOf<String, String>()
        flatten(section, "", map)
        return map
    }

    private fun flatten(section: ConfigurationSection, prefix: String, map: MutableMap<String, String>) {
        for (key in section.getKeys(false)) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            val value = section.get(key)
            if (value is ConfigurationSection) {
                flatten(value, fullKey, map)
            } else {
                map[fullKey] = value.toString()
            }
        }
    }

    private fun appendMissingKeys(file: File, missing: Map<String, String>) {
        val sb = StringBuilder()
        if (file.length() > 0) sb.append("\n")
        missing.forEach { (key, value) ->
            sb.append(key).append(": ")
            if (value.contains('\n')) {
                sb.append("|-\n")
                value.lines().forEach { line -> sb.append("  ").append(line).append('\n') }
            } else {
                sb.append('"').append(value.replace("\\", "\\\\").replace("\"", "\\\"")).append('"').append('\n')
            }
        }
        file.appendText(sb.toString(), Charsets.UTF_8)
    }
}
