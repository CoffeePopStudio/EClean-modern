package top.e404.eclean.lang

import top.e404.eclean.PL
import java.io.File

object LegacyLangMigrator {
    private val legacyPattern = Regex("&[0-9a-fk-or]")
    private val colorMap = mapOf(
        "&0" to "black",
        "&1" to "dark_blue",
        "&2" to "dark_green",
        "&3" to "dark_aqua",
        "&4" to "dark_red",
        "&5" to "dark_purple",
        "&6" to "gold",
        "&7" to "gray",
        "&8" to "dark_gray",
        "&9" to "blue",
        "&a" to "green",
        "&b" to "aqua",
        "&c" to "red",
        "&d" to "light_purple",
        "&e" to "yellow",
        "&f" to "white",
    )
    private val formatMap = mapOf(
        "&l" to "b",
        "&n" to "u",
        "&o" to "i",
        "&m" to "st",
        "&k" to "obfuscated",
        "&r" to "reset",
    )

    fun migrateIfNeeded(file: File, content: String, onMigrated: (String) -> Unit): Boolean {
        if (!legacyPattern.containsMatchIn(content)) return false

        val backup = File(file.parent, "lang.old.yml")
        file.renameTo(backup)

        val migrated = content.lines().map { line ->
            if (line.contains(':')) {
                val colonIndex = line.indexOf(':')
                val key = line.substring(0, colonIndex)
                val value = line.substring(colonIndex + 1).trim()
                if (value.isNotEmpty() && legacyPattern.containsMatchIn(value)) {
                    "$key: \"${legacyToMiniMessage(value)}\""
                } else {
                    line
                }
            } else {
                line
            }
        }.joinToString("\n")

        file.writeText(migrated, Charsets.UTF_8)
        backup.delete()
        PL.logger.info("lang.yml migrated to MiniMessage format")
        onMigrated(migrated)
        return true
    }

    internal fun legacyToMiniMessage(input: String): String {
        val codes = Regex("&[0-9a-fk-or]").findAll(input).toList()
        if (codes.isEmpty()) return input

        val segments = mutableListOf<String>()
        var lastEnd = 0
        for (code in codes) {
            val start = code.range.first
            if (start > lastEnd) {
                segments.add(input.substring(lastEnd, start))
            }
            segments.add(code.value)
            lastEnd = code.range.last + 1
        }
        if (lastEnd < input.length) {
            segments.add(input.substring(lastEnd))
        }

        val result = StringBuilder()
        val openTags = mutableListOf<String>()

        for (i in segments.indices) {
            val seg = segments[i]
            if (seg.startsWith("&") && seg.length == 2) {
                val tag = colorMap[seg]
                if (tag != null) {
                    while (openTags.isNotEmpty()) {
                        result.append("</").append(openTags.removeAt(openTags.size - 1)).append(">")
                    }
                    result.append("<$tag>")
                    openTags.add(tag)
                } else {
                    val fmt = formatMap[seg]
                    if (fmt != null) {
                        result.append("<$fmt>")
                        openTags.add(fmt)
                    }
                }
            } else {
                result.append(seg)
            }
        }

        while (openTags.isNotEmpty()) {
            result.append("</").append(openTags.removeAt(openTags.size - 1)).append(">")
        }

        return result.toString()
    }
}
