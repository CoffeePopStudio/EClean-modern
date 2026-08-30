package top.e404.eclean.command

import org.bukkit.command.CommandSender
import top.e404.eclean.PL
import top.e404.eclean.feature.stats.WorldStatsService
import top.e404.eclean.lang.MLang

object StatusCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("eclean.admin")) return
        when {
            args.size == 2 && args[1].equals("all", true) -> sendAllStatus(sender)
            args.size == 2 -> sender.sendWorldStats(args[1])
            else -> Commands.sendUsage(sender)
        }
    }

    private fun sendAllStatus(sender: CommandSender) {
        val service = WorldStatsService()
        service.collectAllWorldStats { results ->
            if (results.isEmpty()) {
                PL.services.messages.send(sender, MLang["command.stats.empty"])
                return@collectAllWorldStats
            }
            PL.services.messages.send(sender, MLang["command.status_header"])
            results.forEach { (worldName, result) ->
                val command = "/eclean status $worldName"
                val content = MLang[
                    "command.status_world",
                    "world" to worldName,
                    "entities" to result.totalEntities,
                    "chunks" to result.loadedChunks,
                    "force" to result.forceLoadedChunks,
                ]
                PL.services.messages.send(
                    sender,
                    "<click:run_command:'$command'><hover:show_text:'${MLang["common.hover.view_distribution"]}'>$content</hover></click>",
                )
            }
            val total = results.sumOf { it.second.totalEntities }
            PL.services.messages.send(sender, MLang["command.status_total", "total" to total])
        }
    }
}
