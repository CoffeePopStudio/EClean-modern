package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config
import top.e404.eclean.lang.MLang

object DebugCommand {
    fun handle(sender: CommandSender) {
        if (!sender.hasPermission("eclean.admin")) return
        if (sender !is Player) {
            if (Config.current.global.debug) {
                Config.update { it.copy(global = it.global.copy(debug = false)) }
                RuntimeServices.messages.send(sender, MLang["debug.console_disable"])
            } else {
                Config.update { it.copy(global = it.global.copy(debug = true)) }
                RuntimeServices.messages.send(sender, MLang["debug.console_enable"])
            }
            return
        }
        val senderName = sender.name
        if (senderName in RuntimeServices.messages.debuggers) {
            RuntimeServices.messages.debuggers.remove(senderName)
            RuntimeServices.messages.send(sender, MLang["debug.player_disable"])
        } else {
            RuntimeServices.messages.debuggers.add(senderName)
            RuntimeServices.messages.send(sender, MLang["debug.player_enable"])
        }
    }
}
