package top.e404.eclean.lang

import org.yaml.snakeyaml.Yaml
import top.e404.eclean.util.placeholder
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

/**
 * Loader-agnostic language manager.
 *
 * Owns the complete language pipeline in common:
 * - copies bundled language defaults into the platform data directory
 * - migrates legacy `lang.yml`
 * - merges missing keys into user files
 * - flattens YAML into a key -> template cache
 * - resolves templates with MiniMessage-style placeholders
 *
 * It intentionally uses only JVM/Adventure APIs and never touches Bukkit,
 * Minecraft, Fabric or NeoForge.
 */
class LanguageManager(
    private val dataDirectory: Path,
    private val bundledLanguages: List<String> = listOf("zh_cn", "en_us"),
    private val logger: (String) -> Unit = {},
) {
    companion object {
        private const val DEFAULT_LANGUAGE = "zh_cn"
        private val yaml = Yaml()
    }

    @Volatile
    private var cache: Map<String, String> = emptyMap()

    @Volatile
    var currentLanguage: String = DEFAULT_LANGUAGE
        private set

    operator fun get(key: String, vararg placeholder: Pair<String, Any?>): String {
        val raw = cache[key] ?: key
        return raw.placeholder(*placeholder)
    }

    fun load(language: String = DEFAULT_LANGUAGE) {
        val selected = language.ifBlank { DEFAULT_LANGUAGE }
        currentLanguage = selected

        val langDir = dataDirectory.resolve("lang")
        Files.createDirectories(langDir)

        bundledLanguages.forEach { ensureDefaultFile(langDir, it) }

        // Legacy lang.yml migration: if the new file does not exist yet,
        // copy the old single-file language file over first.
        val legacy = dataDirectory.resolve("lang.yml")
        val target = langDir.resolve("$selected.yml")
        if (!Files.exists(target) && Files.exists(legacy)) {
            Files.copy(legacy, target, StandardCopyOption.REPLACE_EXISTING)
        }

        ensureDefaultFile(langDir, selected)
        val targetText = Files.readString(target, Charsets.UTF_8)
        LegacyLangMigrator.migrateIfNeeded(target, targetText, logger = logger)
        mergeMissingKeys(target, selected)
        readIntoCache(target, selected)
    }

    fun reload(language: String = DEFAULT_LANGUAGE) {
        load(language)
    }

    fun put(key: String, value: String) {
        cache = cache + (key to value)
    }

    internal fun flattenToMap(text: String): Map<String, String> {
        val root = yaml.load<Map<String, Any?>>(text) ?: emptyMap()
        val result = mutableMapOf<String, String>()
        flatten(root, "", result)
        return result
    }

    private fun ensureDefaultFile(langDir: Path, language: String) {
        val target = langDir.resolve("$language.yml")
        if (Files.exists(target)) return

        val resource = resolveBundledResource(language) ?: return
        resource.use { input ->
            Files.createDirectories(langDir)
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun resolveBundledResource(language: String): InputStream? =
        LanguageManager::class.java.classLoader.getResourceAsStream("lang/$language.yml")

    private fun readBundledText(language: String): String? =
        resolveBundledResource(language)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }

    private fun mergeMissingKeys(file: Path, language: String) {
        val defaultText = readBundledText(language) ?: return
        val defaultMap = flattenToMap(defaultText)
        val userMap = flattenToMap(Files.readString(file, Charsets.UTF_8))
        val missing = defaultMap.filterKeys { it !in userMap }
        if (missing.isEmpty()) return
        appendMissingKeys(file, missing)
    }

    private fun readIntoCache(file: Path, language: String) {
        val text = try {
            Files.readString(file, Charsets.UTF_8)
        } catch (e: Exception) {
            logger("lang/${file.fileName} read failed, falling back to default: ${e.message}")
            readBundledText(language) ?: return
        }

        cache = try {
            flattenToMap(text)
        } catch (e: Exception) {
            logger("lang/${file.fileName} parse failed, falling back to default: ${e.message}")
            readBundledText(language)?.let { flattenToMap(it) } ?: emptyMap()
        }
    }

    private fun flatten(node: Map<String, Any?>, prefix: String, target: MutableMap<String, String>) {
        for ((key, value) in node) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            when (value) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    flatten(value as Map<String, Any?>, fullKey, target)
                }
                null -> target[fullKey] = ""
                else -> target[fullKey] = value.toString()
            }
        }
    }

    private fun appendMissingKeys(file: Path, missing: Map<String, String>) {
        val sb = StringBuilder()
        if (Files.size(file) > 0) sb.append("\n")
        missing.forEach { (key, value) ->
            sb.append(key).append(": ")
            if (value.contains('\n')) {
                sb.append("|-\n")
                value.lines().forEach { line -> sb.append("  ").append(line).append('\n') }
            } else {
                sb.append('"').append(value.replace("\\", "\\\\").replace("\"", "\\\"")).append('"').append('\n')
            }
        }
        Files.writeString(file, sb.toString(), Charsets.UTF_8, StandardOpenOption.APPEND)
    }
}