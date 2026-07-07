package top.e404.eclean.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.feature.cleanup.chunk.ChunkDensityScanner
import top.e404.eclean.config.Lang
import top.e404.eclean.menu.MenuManager
import top.e404.eclean.menu.dense.DenseMenu
import top.e404.eclean.menu.dense.EntityInfo
import top.e404.eplugin.command.ECommand

object Show : ECommand(
    PL,
    "show",
    "(?i)s|show",
    true,
    "eclean.admin"
) {
    override val usage get() = Lang["command.usage.show"]
    private val scanner by lazy { ChunkDensityScanner() }

    override fun onCommand(sender: CommandSender, args: Array<out String>) {
        sender as Player
        RuntimeServices.scheduler.runGlobal {
            val data = scanner.scanDenseEntries()
                .map { EntityInfo(it.entityType, it.amount, it.chunk) }
                .toMutableList()
            MenuManager.openMenu(DenseMenu(data), sender)
        }
    }
}
