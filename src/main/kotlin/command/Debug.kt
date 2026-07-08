package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config
import top.e404.eclean.config.Lang
import top.e404.eplugin.command.ECommand

object Debug : ECommand(
    PL,
    "debug",
    "(?i)d|debug",
    false,
    "eclean.admin"
) {
    override val usage get() = Lang["command.usage.debug"]

    override fun onCommand(
        sender: CommandSender,
        args: Array<out String>,
    ) {
        if (sender !is Player) {
            if (Config.current.global.debug) {
                Config.update { it.copy(global = it.global.copy(debug = false)) }
                RuntimeServices.messages.send(sender, Lang["debug.console_disable"])
            } else {
                Config.update { it.copy(global = it.global.copy(debug = true)) }
                RuntimeServices.messages.send(sender, Lang["debug.console_enable"])
            }
            return
        }
        val senderName = sender.name
        if (senderName in RuntimeServices.messages.debuggers) {
            RuntimeServices.messages.debuggers.remove(senderName)
            RuntimeServices.messages.send(sender, Lang["debug.player_disable"])
        } else {
            RuntimeServices.messages.debuggers.add(senderName)
            RuntimeServices.messages.send(sender, Lang["debug.player_enable"])
        }
    }
}
