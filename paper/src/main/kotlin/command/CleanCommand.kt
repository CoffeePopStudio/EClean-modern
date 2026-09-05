package top.e404.eclean.command

import org.bukkit.command.CommandSender
import top.e404.eclean.PL

object CleanCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        cleanCommandHandler(
            messageProvider = PaperMessageProvider(),
            cleanupService = PL.services.commonPlatform.cleanupCommandService,
        )(sender.toCommon(), args)
    }
}
