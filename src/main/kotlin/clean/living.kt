package top.e404.eclean.clean

import org.bukkit.Bukkit
import org.bukkit.World
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config
import top.e404.eclean.feature.cleanup.living.LivingCleanupService
import top.e404.eclean.util.noOnline
import top.e404.eclean.util.noOnlineMessage
import top.e404.eplugin.EPlugin.Companion.placeholder

private inline val livingCfg get() = Config.config.living
private val livingService by lazy { LivingCleanupService() }

/**
 * 最近一次清理生物实体的数量
 */
var lastLiving = 0
    private set

/**
 * 清理全服生物
 */
fun cleanLiving(announce: Boolean = true) {
    if (!livingCfg.enable) {
        PL.debug { "生物清理已禁用" }
        return
    }
    val worlds = Bukkit.getWorlds().filterNot { livingCfg.disableWorld.any { regex -> it.name matches regex } }
    PL.buildDebug {
        append("开始清理生物, 启用生物清理的世界: [")
        worlds.joinTo(this, ", ", transform = World::getName)
        append("]")
    }
    PL.debug { if (livingCfg.settings.name) "清理被命名的生物" else "不清理被命名的生物" }
    PL.debug { if (livingCfg.settings.lead) "清理拴绳拴住的生物" else "不清理拴绳拴住的生物" }
    PL.debug { if (livingCfg.settings.mount) "清理乘骑中的生物" else "不清理乘骑中的生物" }

    var time = System.currentTimeMillis()
    val result = worlds.map { livingService.cleanWorld(it) }
    time = System.currentTimeMillis() - time

    lastLiving = result.sumOf { it.cleaned }
    RuntimeServices.statusSnapshots.updateCleanup { it.copy(lastLiving = lastLiving) }
    PL.debug { "生物清理共${lastLiving}个, 耗时${time}ms" }
    if (!announce) return

    if (noOnline) {
        if (noOnlineMessage) {
            val all = result.sumOf { it.total }
            val finish = livingCfg.finish
            if (finish.isNotBlank()) RuntimeServices.cleanupAnnouncementService.announceLivingFinish(
                finish.placeholder(mapOf("clean" to lastLiving, "all" to all))
            )
        }
    } else {
        val all = result.sumOf { it.total }
        val finish = livingCfg.finish
        if (finish.isNotBlank()) RuntimeServices.cleanupAnnouncementService.announceLivingFinish(
            finish.placeholder(mapOf("clean" to lastLiving, "all" to all))
        )
    }
}

/**
 * 清理指定世界的生物
 *
 * @return Pair(clean, all)
 */
fun World.cleanLiving(): Pair<Int, Int> {
    val result = livingService.cleanWorld(this)
    return result.cleaned to result.total
}
