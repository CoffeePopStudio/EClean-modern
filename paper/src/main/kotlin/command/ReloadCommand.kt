package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.PL
import top.e404.eclean.config.Config
import java.util.UUID

object ReloadCommand {
    fun handle(sender: CommandSender) {
        val common = sender.toCommon()
        reloadCommandHandler(
            messageProvider = PaperMessageProvider(),
            onReload = { PL.services.reload(sender) }
        )(common, emptyArray())
    }
}
