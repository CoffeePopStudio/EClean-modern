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
        if (!file.renameTo(backup)) {
            PL.logger.warning("Failed to backup lang.yml, migration aborted")
            return false
        }

        val lines = content.lines()
        val result = mutableListOf<String>()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            // Skip comments and lines without ':'
            val trimmed = line.trimStart()
            if (trimmed.startsWith("#") || !line.contains(':')) {
                result.add(line)
                i++
                continue
            }

            val colonIndex = line.indexOf(':')
            val afterColon = line.substring(colonIndex + 1)

            // Detect block scalar indicator: ": |", ": |-", ": >", ": >-", etc.
            if (afterColon.trimStart().matches(Regex("[|>][-+]?\\d*\\s*"))) {
                val keyPart = line.substring(0, colonIndex + 1)
                val indicator = afterColon.trimStart()
                val blockLines = mutableListOf<String>()
                i++

                // Determine base indentation from first non-blank content line
                var baseIndent = Int.MAX_VALUE
                var j = i
                while (j < lines.size) {
                    val nl = lines[j]
                    if (nl.isBlank()) {
                        j++
                        continue
                    }
                    val indent = nl.takeWhile { it == ' ' }.length
                    if (indent < baseIndent) baseIndent = indent
                    if (indent == 0 && nl.isNotBlank()) break // back to root level
                    j++
                }

                if (baseIndent == Int.MAX_VALUE) baseIndent = 0

                while (i < lines.size) {
                    val nextLine = lines[i]
                    if (nextLine.isBlank()) {
                        blockLines.add(nextLine)
                        i++
                        continue
                    }
                    val nextIndent = nextLine.takeWhile { it == ' ' }.length
                    if (nextIndent >= baseIndent) {
                        blockLines.add(nextLine)
                        i++
                    } else {
                        break
                    }
                }

                val blockContent = blockLines.joinToString("\n")
                if (legacyPattern.containsMatchIn(blockContent)) {
                    val migratedBlock = blockLines.joinToString("\n") { bl ->
                        if (bl.isBlank()) bl
                        else {
                            val contentStart = bl.indexOfFirst { it != ' ' }
                            val indent = bl.substring(0, contentStart)
                            "$indent${legacyToMiniMessage(bl.substring(contentStart))}"
                        }
                    }
                    result.add("$keyPart $indicator\n$migratedBlock")
                } else {
                    result.add(line)
                    result.addAll(blockLines)
                }
            } else {
                // Regular key: value pair
                val value = afterColon.trim()
                if (value.isNotEmpty() && legacyPattern.containsMatchIn(value)) {
                    // Strip existing YAML quotes before converting, then re-quote
                    val unquoted = value.removeSurrounding("\"").removeSurrounding("'")
                    val keyStr = line.substring(0, colonIndex)
                    result.add("$keyStr: \"${legacyToMiniMessage(unquoted)}\"")
                } else {
                    result.add(line)
                }
                i++
            }
        }

        val migrated = result.joinToString("\n")
        file.writeText(migrated, Charsets.UTF_8)
        backup.delete()
        PL.logger.info("lang.yml migrated to MiniMessage format")
        onMigrated(migrated)
        return true
    }

    internal fun legacyToMiniMessage(input: String): String {
        val codes = legacyPattern.findAll(input).toList()
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

        for (seg in segments) {
            if (seg.startsWith("&") && seg.length == 2) {
                val tag = colorMap[seg]
                if (tag != null) {
                    // Color code: close all open tags (Minecraft behavior — color resets formatting)
                    while (openTags.isNotEmpty()) {
                        result.append("</").append(openTags.removeAt(openTags.size - 1)).append(">")
                    }
                    result.append("<$tag>")
                    openTags.add(tag)
                } else {
                    val fmt = formatMap[seg]
                    if (fmt != null) {
                        if (fmt == "reset") {
                            // <reset> is a standalone tag in MiniMessage — close all open tags,
                            // emit <reset>, and do NOT add to openTags (no </reset> counterpart)
                            while (openTags.isNotEmpty()) {
                                result.append("</").append(openTags.removeAt(openTags.size - 1)).append(">")
                            }
                            result.append("<reset>")
                        } else {
                            // Format code: stack (Minecraft behavior — format codes stack)
                            result.append("<$fmt>")
                            openTags.add(fmt)
                        }
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
