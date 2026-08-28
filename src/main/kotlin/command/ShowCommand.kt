package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.feature.cleanup.chunk.ChunkDensityScanner
import top.e404.eclean.platform.Schedulers

object ShowCommand {
    fun handle(sender: CommandSender) {
        if (sender !is Player) return
        if (!sender.hasPermission("eclean.admin")) return
        val scanner = ChunkDensityScanner()
        scanner.scanDenseEntries { entries ->
            val data = entries
                .map { top.e404.eclean.menu.dense.EntityInfo(it.entityType, it.amount, it.chunk) }
                .toMutableList()
            Schedulers.runGlobal {
                top.e404.eclean.menu.dense.DenseMenu(data).open(sender)
            }
        }
    }
}
