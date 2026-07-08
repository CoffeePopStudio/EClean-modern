package top.e404.eclean.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Lang
import top.e404.eclean.feature.stats.WorldStatsService
import top.e404.eplugin.EPlugin.Companion.formatAsConst

fun CommandSender.sendWorldStats(worldName: String) {
    val world = Bukkit.getWorld(worldName)
    if (world == null) {
        PL.sendMsgWithPrefix(this, "&c不存在名为&e$worldName&c的世界")
        return
    }
    val service = WorldStatsService(RuntimeServices.scheduler)
    service.collectWorldStats(worldName) { result ->
        if (result == null) {
            PL.sendMsgWithPrefix(this, "&c收集世界统计信息失败")
            return@collectWorldStats
        }
        if (result.totalEntities == 0) {
            PL.sendMsgWithPrefix(this, Lang["command.stats.empty"])
            return@collectWorldStats
        }
        val entity = result.sortedEntries().joinToString(Lang["command.stats.spacing"]) { (k, v) ->
            Lang[
                "command.stats.content",
                "type" to k,
                "count" to v.withColor()
            ]
        }
        PL.sendMsgWithPrefix(
            this,
            Lang[
                "command.stats.world",
                "world" to worldName,
                "count" to result.loadedChunks,
                "force" to result.forceLoadedChunks,
                "entity" to entity
            ]
        )
    }
}

fun CommandSender.sendEntityStats(worldName: String, typeName: String, min: Int = 0) {
    val world = Bukkit.getWorld(worldName)
    if (world == null) {
        PL.sendMsgWithPrefix(this, "&c不存在名为&e${worldName}&c的世界")
        return
    }
    val type = try {
        EntityType.valueOf(typeName.formatAsConst())
    } catch (t: Throwable) {
        PL.sendMsgWithPrefix(this, Lang["message.invalid_entity_type"])
        return
    }
    val service = WorldStatsService(RuntimeServices.scheduler)
    service.collectEntityStats(worldName, type, min) { entries ->
        if (entries.isEmpty()) {
            PL.sendMsgWithPrefix(this, Lang["command.stats.empty"])
            return@collectEntityStats
        }
        val entity = entries.joinToString(Lang["command.stats.spacing"]) { (label, v) ->
            Lang[
                "command.stats.content",
                "type" to label,
                "count" to v.withColor()
            ]
        }
        PL.sendMsgWithPrefix(
            this,
            Lang[
                "command.stats.entity",
                "type" to typeName,
                "entity" to entity
            ]
        )
    }
}

private fun Int.withColor() = when {
    this > 60 -> "&c$this"
    this > 30 -> "&e$this"
    else -> "&a$this"
}
