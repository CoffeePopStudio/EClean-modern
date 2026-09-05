package top.e404.eclean.command

import org.bukkit.command.CommandSender
import top.e404.eclean.PL

object StatusCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        statusCommandHandler(
            messageProvider = PaperMessageProvider(),
            worldStatsProvider = PL.services.commonPlatform.worldStatsProvider,
        )(sender.toCommon(), args)
    }
}
