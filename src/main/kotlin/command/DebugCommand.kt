package top.e404.eclean.command
import top.e404.eclean.PL

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.config.Config
import top.e404.eclean.lang.MLang

object DebugCommand {
    fun handle(sender: CommandSender) {
        if (!sender.hasPermission("eclean.admin")) return
        if (sender !is Player) {
            if (Config.current.global.debug) {
                Config.update { it.copy(global = it.global.copy(debug = false)) }
                PL.services.messages.send(sender, MLang["debug.console_disable"])
            } else {
                Config.update { it.copy(global = it.global.copy(debug = true)) }
                PL.services.messages.send(sender, MLang["debug.console_enable"])
            }
            return
        }
        val senderName = sender.name
        if (senderName in PL.services.messages.debuggers) {
            PL.services.messages.debuggers.remove(senderName)
            PL.services.messages.send(sender, MLang["debug.player_disable"])
        } else {
            PL.services.messages.debuggers.add(senderName)
            PL.services.messages.send(sender, MLang["debug.player_enable"])
        }
    }
}
