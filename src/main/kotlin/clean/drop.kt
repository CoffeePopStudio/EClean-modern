package top.e404.eclean.clean

import org.bukkit.Bukkit
import org.bukkit.World
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.ModernConfig
import top.e404.eclean.feature.cleanup.drop.DropCleanupService
import top.e404.eclean.util.noOnline
import top.e404.eclean.util.noOnlineMessage
import top.e404.eplugin.EPlugin.Companion.placeholder

private inline val dropCfg get() = ModernConfig.drop
private val dropService by lazy { DropCleanupService() }


/**
 * 最近一次清理掉落物的数量
 */
var lastDrop = 0
    private set

/**
 * 清理全服掉落物
 */
fun cleanDrop(announce: Boolean = true) {
    if (!dropCfg.enabled) {
        PL.debug { "掉落物清理已禁用" }
        return
    }
    val worlds = Bukkit.getWorlds().filterNot { dropCfg.disabledWorlds.any { regex -> it.name matches regex } }
    PL.buildDebug {
        append("开始清理掉落物, 启用掉落物清理的世界: [")
        worlds.joinTo(this, ", ", transform = World::getName)
        append("]")
    }
    PL.debug { if (dropCfg.protectEnchanted) "不清理附魔的物品" else "清理附魔的物品" }
    PL.debug { if (dropCfg.protectWrittenBook) "不清理成书" else "清理成书" }

    var time = System.currentTimeMillis()
    val result = worlds.map { dropService.cleanWorld(it) }
    time = System.currentTimeMillis() - time

    lastDrop = result.sumOf { it.cleaned }
    RuntimeServices.statusSnapshots.updateCleanup { it.copy(lastDrop = lastDrop) }
    PL.debug { "掉落物清理共${lastDrop}个, 耗时${time}ms" }
    if (!announce) return

    if (noOnline) {
        if (noOnlineMessage) {
            val all = result.sumOf { it.total }
            val finish = dropCfg.finishMessage
            if (finish.isNotBlank()) RuntimeServices.cleanupAnnouncementService.announceDropFinish(
                finish.placeholder("clean" to lastDrop, "all" to all)
            )
        }
    } else {
        val all = result.sumOf { it.total }
        val finish = dropCfg.finishMessage
        if (finish.isNotBlank()) RuntimeServices.cleanupAnnouncementService.announceDropFinish(
            finish.placeholder("clean" to lastDrop, "all" to all)
        )
    }
}

/**
 * 清理指定世界的掉落物
 *
 * @return Pair(clean, all)
 */
fun World.cleanDrop(): Pair<Int, Int> {
    val result = dropService.cleanWorld(this)
    return result.cleaned to result.total
}
