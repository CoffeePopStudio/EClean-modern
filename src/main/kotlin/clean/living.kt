package top.e404.eclean.clean

import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.ModernConfig
import top.e404.eclean.feature.cleanup.living.LivingCleanupResult
import top.e404.eclean.feature.cleanup.living.LivingCleanupService
import top.e404.eclean.util.noOnline
import top.e404.eclean.util.noOnlineMessage
import top.e404.eclean.util.placeholder

private inline val livingCfg get() = ModernConfig.living

private fun resolveLivingService(): LivingCleanupService = LivingCleanupService()

var lastLiving = 0
    private set

fun cleanLiving(announce: Boolean = true, onComplete: ((Int) -> Unit)? = null) {
    if (!livingCfg.enabled) {
        RuntimeServices.messages.debug { "Living entity cleanup is disabled" }
        onComplete?.invoke(0)
        return
    }
    val service = resolveLivingService()
    RuntimeServices.messages.debug { "Starting living entity cleanup" }
    RuntimeServices.messages.debug { if (livingCfg.settings.cleanNamed) "Clean named entities" else "Skip named entities" }
    RuntimeServices.messages.debug { if (livingCfg.settings.cleanLeashed) "Clean leashed entities" else "Skip leashed entities" }
    RuntimeServices.messages.debug { if (livingCfg.settings.cleanMounted) "Clean mounted entities" else "Skip mounted entities" }

    val time = System.currentTimeMillis()
    service.cleanAllWorlds { results ->
        val elapsed = System.currentTimeMillis() - time
        lastLiving = results.sumOf { it.cleaned }
        RuntimeServices.statusSnapshots.updateCleanup { it.copy(lastLiving = lastLiving) }
        RuntimeServices.messages.debug { "Living entity cleanup finished: ${lastLiving} removed, ${elapsed}ms" }
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
