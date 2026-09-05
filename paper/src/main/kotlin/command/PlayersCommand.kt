package top.e404.eclean.command

import org.bukkit.command.CommandSender
import top.e404.eclean.PL

object PlayersCommand {
    fun handle(sender: CommandSender) {
        playersCommandHandler(
            messageProvider = PaperMessageProvider(),
            playerProvider = PL.services.commonPlatform.playerProvider,
        )(sender.toCommon(), emptyArray())
    }
}
