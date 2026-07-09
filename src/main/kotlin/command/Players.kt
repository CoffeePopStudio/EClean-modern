package top.e404.eclean.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import top.e404.eclean.lang.MLang
import top.e404.eclean.platform.Schedulers
import java.util.concurrent.atomic.AtomicInteger

fun CommandSender.sendPlayersStats() {
    Schedulers.runGlobal {
        val players = Bukkit.getOnlinePlayers().toList()
        if (players.isEmpty()) {
            sendMessage(MLang["command.stats.empty"])
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
                    val entry = "  &b${player.name}&f: ${loc.blockX} ${loc.blockY} ${loc.blockZ}"
                    synchronized(worldLines) { worldLines += entry }
                    if (pending.decrementAndGet() == 0) sendPlayerResult(this@sendPlayersStats, lines)
                }
            }
        }
    }
}

private fun sendPlayerResult(sender: CommandSender, lines: Map<String, List<String>>) {
    Schedulers.runGlobal {
        lines.forEach { (worldName, worldLines) ->
            sender.sendMessage("&6${worldName}:${worldLines.joinToString("")}")
        }
    }
}
