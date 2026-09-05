package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.PL

object ShowCommand {
    fun handle(sender: CommandSender) {
        val common = if (sender is Player) sender.toCommonPlayer() else sender.toCommon()
        showCommandHandler(
            messageProvider = PaperMessageProvider(),
            denseShowService = PL.services.commonPlatform.denseShowService,
        )(common, emptyArray())
    }
}
