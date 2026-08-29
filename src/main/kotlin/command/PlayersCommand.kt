package top.e404.eclean.command

import org.bukkit.command.CommandSender

object PlayersCommand {
    fun handle(sender: CommandSender) {
        if (!sender.hasPermission("eclean.admin")) return
        sender.sendPlayersStats()
    }
}
