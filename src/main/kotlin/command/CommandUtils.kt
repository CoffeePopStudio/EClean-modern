package top.e404.eclean.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.feature.stats.WorldStatsService
import top.e404.eclean.lang.MLang
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.util.formatAsConst
import top.e404.eclean.util.parseSecondAsDuration
import java.util.concurrent.atomic.AtomicInteger

internal fun CommandSender.sendTrashStats() {
    val entries = RuntimeServices.trashcanManager.stats()
    if (entries.isEmpty()) {
        RuntimeServices.messages.send(this, MLang["command.trash_stats_empty"])
        return
    }
    RuntimeServices.messages.send(this, MLang["command.trash_stats_header", "count" to entries.size])
    val now = System.currentTimeMillis()
    for (entry in entries) {
        val expire = entry.deadline.takeIf { it != Long.MAX_VALUE }
            ?.let { maxOf(0, (it - now) / 1000) }
            ?.parseSecondAsDuration()
            ?: MLang["command.trash_never_expire"]
        RuntimeServices.messages.send(
            this,
            MLang["command.trash_stats_line", "item" to entry.prototype.type.name, "amount" to entry.count, "expire" to expire],
        )
    }
}

internal fun CommandSender.sendWorldStats(worldName: String) {
    val world = Bukkit.getWorld(worldName)
    if (world == null) {
        RuntimeServices.messages.send(this, MLang["command.invalid_world", "world" to worldName])
        return
    }
    val service = WorldStatsService()
    service.collectWorldStats(worldName) { result ->
        if (result == null) {
            RuntimeServices.messages.send(this, MLang["command.stats_collect_failed"])
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

internal fun CommandSender.sendEntityStats(worldName: String, typeName: String, min: Int = 0) {
    val world = Bukkit.getWorld(worldName)
    if (world == null) {
        RuntimeServices.messages.send(this, MLang["command.invalid_world", "world" to worldName])
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

internal fun CommandSender.sendPlayersStats() {
    Schedulers.runGlobal {
        val players = Bukkit.getOnlinePlayers().toList()
        if (players.isEmpty()) {
            RuntimeServices.messages.send(this, MLang["command.stats.empty"])
            return@runGlobal
        }
        val byWorld = players.groupBy { it.world.name to it.world }
        val pending = AtomicInteger(players.size)
        val lines = LinkedHashMap<String, MutableList<String>>()
        byWorld.values.forEach { list ->
            val worldName = list.first().world.name
            val worldLines = mutableListOf<String>()
            synchronized(lines) { lines[worldName] = worldLines }
            list.forEach { player ->
                Schedulers.runForEntity(player) {
                    val loc = player.location
                    val entry = MLang[
                        "command.player_location",
                        "player" to player.name,
                        "x" to loc.blockX,
                        "y" to loc.blockY,
                        "z" to loc.blockZ,
                    ]
                    synchronized(worldLines) { worldLines += entry }
                    if (pending.decrementAndGet() == 0) sendPlayerResult(this@sendPlayersStats, lines)
                }
            }
        }
    }
}

internal fun sendPlayerResult(sender: CommandSender, lines: Map<String, List<String>>) {
    Schedulers.runGlobal {
        lines.forEach { (worldName, worldLines) ->
            RuntimeServices.messages.send(
                sender,
                MLang["command.players_header", "world" to worldName, "lines" to worldLines.joinToString("")],
            )
        }
    }
}

internal fun Int.withColor() = when {
    this > 60 -> "<red>$this</red>"
    this > 30 -> "<yellow>$this</yellow>"
    else -> "<green>$this</green>"
}
