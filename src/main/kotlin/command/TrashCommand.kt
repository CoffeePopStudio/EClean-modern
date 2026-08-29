package top.e404.eclean.command
import top.e404.eclean.PL

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.config.Config
import top.e404.eclean.lang.MLang

object TrashCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        if (args.size == 2 && args[1].equals("stats", true)) {
            if (!sender.hasPermission("eclean.admin")) return
            sender.sendTrashStats()
            return
        }
        if (sender !is Player) return
        if (!sender.hasPermission("eclean.trash")) return
        if (!Config.current.trashcan.enabled) {
            PL.services.messages.send(sender, MLang["command.trash_disable"])
            return
        }
        top.e404.eclean.clean.Trashcan.open(sender)
        PL.services.messages.send(sender, MLang["command.trash_open"])
    }
}
