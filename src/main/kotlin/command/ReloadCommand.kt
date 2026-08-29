package top.e404.eclean.command
import top.e404.eclean.PL

import org.bukkit.command.CommandSender

object ReloadCommand {
    fun handle(sender: CommandSender) {
        PL.services.reload(sender)
    }
}
