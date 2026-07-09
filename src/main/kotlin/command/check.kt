package top.e404.eclean.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.lang.MLang
import top.e404.eclean.feature.stats.WorldStatsService
import top.e404.eclean.util.formatAsConst

fun CommandSender.sendWorldStats(worldName: String) {
    val world = Bukkit.getWorld(worldName)
    if (world == null) {
        RuntimeServices.messages.send(this, "&c不存在名为&e$worldName&c的世界")
        return
    }
    val service = WorldStatsService()
    service.collectWorldStats(worldName) { result ->
        if (result == null) {
            RuntimeServices.messages.send(this, "&c收集世界统计信息失败")
            return@collectWorldStats
        }
        if (result.totalEntities == 0) {
            RuntimeServices.messages.send(this, MLang["command.stats.empty"])
            return@collectWorldStats
        }
        val entity = result.sortedEntries().joinToString(MLang["command.stats.spacing"]) { (k, v) ->
            MLang[
                "command.stats.content",
                "type" to k,
                "count" to v.withColor()
            ]
        }
        RuntimeServices.messages.send(
            this,
            MLang[
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
        RuntimeServices.messages.send(this, "&c不存在名为&e${worldName}&c的世界")
        return
    }
    val type = try {
        EntityType.valueOf(typeName.formatAsConst())
    } catch (t: Throwable) {
        RuntimeServices.messages.send(this, MLang["message.invalid_entity_type"])
        return
    }
    val service = WorldStatsService()
    service.collectEntityStats(worldName, type, min) { entries ->
        if (entries.isEmpty()) {
            RuntimeServices.messages.send(this, MLang["command.stats.empty"])
            return@collectEntityStats
        }
        val entity = entries.joinToString(MLang["command.stats.spacing"]) { (label, v) ->
            MLang[
                "command.stats.content",
                "type" to label,
                "count" to v.withColor()
            ]
        }
        RuntimeServices.messages.send(
            this,
            MLang[
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
