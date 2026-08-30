package top.e404.eclean.command

import org.bukkit.command.CommandSender
import top.e404.eclean.PL
import top.e404.eclean.lang.MLang
import java.text.SimpleDateFormat
import java.util.Date

object HistoryCommand {
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun handle(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("eclean.admin")) return
        val limit = args.getOrNull(1)?.toIntOrNull() ?: 10
        val records = PL.services.cleanupHistory.recent(limit)
        if (records.isEmpty()) {
            PL.services.messages.send(sender, MLang["command.history_empty"])
            return
        }
        PL.services.messages.send(sender, MLang["command.history_header"])
        records.forEach { record ->
            PL.services.messages.send(
                sender,
                MLang[
                    "command.history_line",
                    "time" to format.format(Date(record.timestamp)),
                    "drop" to record.drop,
                    "living" to record.living,
                    "chunk" to record.chunk,
                ],
            )
        }
    }
}
