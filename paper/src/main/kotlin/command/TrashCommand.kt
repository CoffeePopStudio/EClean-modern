package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.PL

object TrashCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        val common = if (sender is Player) sender.toCommonPlayer() else sender.toCommon()
        trashCommandHandler(
            messageProvider = PaperMessageProvider(),
            trashcanService = PL.services.commonPlatform.trashcanService,
        )(common, args)
    }
}
