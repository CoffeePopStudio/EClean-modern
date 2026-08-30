package top.e404.eclean.command

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.PL
import top.e404.eclean.lang.MLang

object TeleportCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("eclean.admin")) return
        if (sender !is Player) return
        if (args.size != 5) {
            Commands.sendUsage(sender)
            return
        }
        val world = Bukkit.getWorld(args[1])
        if (world == null) {
            PL.services.messages.send(sender, MLang["command.invalid_world", "world" to args[1]])
            return
        }
        val x = args[2].toDoubleOrNull()
        val y = args[3].toDoubleOrNull()
        val z = args[4].toDoubleOrNull()
        if (x == null || y == null || z == null) {
            val invalid = listOf(args[2], args[3], args[4]).firstOrNull { it.toDoubleOrNull() == null } ?: ""
            PL.services.messages.send(sender, MLang["message.invalid_number", "number" to invalid])
            return
        }
        PL.services.playerTeleportService.teleport(sender, Location(world, x, y, z))
        PL.services.messages.send(sender, MLang["command.teleport.done"])
    }
}
