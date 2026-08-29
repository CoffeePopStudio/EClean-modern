package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object StatsCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("eclean.admin")) return
        when (args.size) {
            1 -> {
                if (sender !is Player) return
                sender.sendWorldStats(sender.world.name)
            }
            2 -> sender.sendWorldStats(args[1])
            else -> Commands.sendUsage(sender)
        }
    }
}
