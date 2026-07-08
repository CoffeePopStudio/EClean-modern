package top.e404.eclean.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Lang
import top.e404.eclean.util.color
import top.e404.eplugin.command.ECommand
import java.util.concurrent.atomic.AtomicInteger

object Players : ECommand(
    PL,
    "players",
    "(?i)p|players?",
    false,
    "eclean.admin"
) {
    override val usage get() = Lang["command.usage.players"]

    override fun onCommand(
        sender: CommandSender,
        args: Array<out String>,
    ) {
        RuntimeServices.scheduler.runGlobal {
            val players = Bukkit.getOnlinePlayers().toList()
            if (players.isEmpty()) {
                sender.sendMessage(Lang["command.stats.empty"].color)
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
                    RuntimeServices.scheduler.runForEntity(player) {
                        val loc = player.location
                        val entry = "  &b${player.name}&f: ${loc.blockX} ${loc.blockY} ${loc.blockZ}"
                        synchronized(worldLines) { worldLines += entry }
                        if (pending.decrementAndGet() == 0) sendResult(sender, lines)
                    }
                }
            }
        }
    }

    private fun sendResult(sender: CommandSender, lines: Map<String, List<String>>) {
        RuntimeServices.scheduler.runGlobal {
            lines.forEach { (worldName, worldLines) ->
                sender.sendMessage("&6${worldName}:${worldLines.joinToString("")}".color)
            }
        }
    }
}
