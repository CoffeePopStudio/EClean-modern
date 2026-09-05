package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.PL

object TeleportCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        val common = if (sender is Player) sender.toCommonPlayer() else sender.toCommon()
        teleportCommandHandler(
            messageProvider = PaperMessageProvider(),
            worldAccess = PL.services.commonPlatform.worldAccess,
            teleportService = PL.services.commonPlatform.teleportService,
        )(common, args)
    }
}
