package top.e404.eclean.clean

import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.ModernConfig
import top.e404.eclean.feature.cleanup.living.LivingCleanupResult
import top.e404.eclean.feature.cleanup.living.LivingCleanupService
import top.e404.eclean.util.noOnline
import top.e404.eclean.util.noOnlineMessage
import top.e404.eplugin.EPlugin.Companion.placeholder

private inline val livingCfg get() = ModernConfig.living

private fun resolveLivingService(): LivingCleanupService? {
    val scheduler = if (RuntimeServices.isSchedulerReady) RuntimeServices.scheduler else null
    return scheduler?.let { LivingCleanupService(it) }
}

var lastLiving = 0
    private set

fun cleanLiving(announce: Boolean = true, onComplete: ((Int) -> Unit)? = null) {
    if (!livingCfg.enabled) {
        PL.debug { "生物清理已禁用" }
        onComplete?.invoke(0)
        return
    }
    val service = resolveLivingService() ?: run {
        onComplete?.invoke(0)
        return
    }
    PL.debug { "开始清理生物" }
    PL.debug { if (livingCfg.settings.cleanNamed) "清理被命名的生物" else "不清理被命名的生物" }
    PL.debug { if (livingCfg.settings.cleanLeashed) "清理拴绳拴住的生物" else "不清理拴绳拴住的生物" }
    PL.debug { if (livingCfg.settings.cleanMounted) "清理乘骑中的生物" else "不清理乘骑中的生物" }

    val time = System.currentTimeMillis()
    service.cleanAllWorlds { results ->
        val elapsed = System.currentTimeMillis() - time
        lastLiving = results.sumOf { it.cleaned }
        RuntimeServices.statusSnapshots.updateCleanup { it.copy(lastLiving = lastLiving) }
        PL.debug { "生物清理共${lastLiving}个, 耗时${elapsed}ms" }
        if (announce) announceLiving(results)
        onComplete?.invoke(lastLiving)
    }
}

private fun announceLiving(results: List<LivingCleanupResult>) {
    val all = results.sumOf { it.total }
    val finish = livingCfg.finishMessage
    if (finish.isBlank()) return
    val message = finish.placeholder(mapOf("clean" to lastLiving, "all" to all))
    if (noOnline && !noOnlineMessage) return
    RuntimeServices.cleanupAnnouncementService.announceLivingFinish(message)
}
