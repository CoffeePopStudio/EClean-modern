package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.lang.MLang

object EntityCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        when (args.size) {
            2 -> {
                if (sender !is Player) return
                sender.sendEntityStats(sender.world.name, args[1])
            }
            3 -> sender.sendEntityStats(args[2], args[1])
            4 -> {
                val min = args[3].toIntOrNull()
                if (min == null) {
                    RuntimeServices.messages.send(sender, MLang["message.invalid_number", "number" to args[3]])
                    return
                }
                sender.sendEntityStats(args[2], args[1], min)
            }
            else -> Commands.sendUsage(sender)
        }
    }
}
