package top.e404.eclean.command

import org.bukkit.command.CommandSender
import top.e404.eclean.app.RuntimeServices

object ReloadCommand {
    fun handle(sender: CommandSender) {
        RuntimeServices.reload(sender)
    }
}
