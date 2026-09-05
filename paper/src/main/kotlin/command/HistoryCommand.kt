package top.e404.eclean.command

import org.bukkit.command.CommandSender
import top.e404.eclean.PL

object HistoryCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        historyCommandHandler(
            messageProvider = PaperMessageProvider(),
            history = PL.services.cleanupHistory,
        )(sender.toCommon(), args)
    }
}
