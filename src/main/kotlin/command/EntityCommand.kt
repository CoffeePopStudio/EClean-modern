package top.e404.eclean.command
import top.e404.eclean.PL

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.lang.MLang

object EntityCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("eclean.admin")) return
        when (args.size) {
            2 -> {
                if (sender !is Player) return
                sender.sendEntityStats(sender.world.name, args[1])
            }
            3 -> sender.sendEntityStats(args[2], args[1])
            4 -> {
                val min = args[3].toIntOrNull()
                if (min == null) {
                    PL.services.messages.send(sender, MLang["message.invalid_number", "number" to args[3]])
                    return
                }
                sender.sendEntityStats(args[2], args[1], min)
            }
            5 -> {
                val chunkX = args[3].toIntOrNull()
                val chunkZ = args[4].toIntOrNull()
                if (chunkX == null || chunkZ == null) {
                    PL.services.messages.send(sender, MLang["message.invalid_number", "number" to if (chunkX == null) args[3] else args[4]])
                    return
                }
                sender.sendEntityStats(args[2], args[1], chunkX = chunkX, chunkZ = chunkZ)
            }
            else -> Commands.sendUsage(sender)
        }
    }
}
