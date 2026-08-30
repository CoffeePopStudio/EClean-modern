package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.feature.stats.WorldStatsService
import top.e404.eclean.menu.MenuManager
import top.e404.eclean.menu.stats.StatsMenu
import top.e404.eclean.platform.Schedulers

object StatsCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("eclean.admin")) return
        when {
            args.size == 2 && args[1].equals("gui", true) -> {
                if (sender !is Player) return
                openStatsGui(sender, sender.world.name)
            }
            args.size == 3 && args[1].equals("gui", true) -> openStatsGui(sender, args[2])
            args.size == 1 -> {
                if (sender !is Player) return
                sender.sendWorldStats(sender.world.name)
            }
            args.size == 2 -> sender.sendWorldStats(args[1])
            else -> Commands.sendUsage(sender)
        }
    }

    private fun openStatsGui(sender: CommandSender, worldName: String) {
        val player = sender as? Player ?: return
        WorldStatsService().collectWorldStats(worldName) { result ->
            if (result == null) return@collectWorldStats
            Schedulers.runGlobal {
                MenuManager.openMenu(StatsMenu(worldName, result.sortedEntries()), player)
            }
        }
    }
}
