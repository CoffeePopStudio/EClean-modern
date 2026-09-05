package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.PL

object StatsCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        val common = if (sender is Player) sender.toCommonPlayer() else sender.toCommon()
        statsCommandHandler(
            messageProvider = PaperMessageProvider(),
            worldStatsProvider = PL.services.commonPlatform.worldStatsProvider,
            statsMenuService = PL.services.commonPlatform.statsMenuService,
        )(common, args)
    }
}
