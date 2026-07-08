package top.e404.eclean.clean

import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.ModernConfig
import top.e404.eclean.feature.cleanup.drop.DropCleanupResult
import top.e404.eclean.feature.cleanup.drop.DropCleanupService
import top.e404.eclean.util.noOnline
import top.e404.eclean.util.noOnlineMessage
import top.e404.eclean.util.placeholder

private inline val dropCfg get() = ModernConfig.drop

private fun resolveDropService(): DropCleanupService? {
    val scheduler = if (RuntimeServices.isSchedulerReady) RuntimeServices.scheduler else null
    return scheduler?.let { DropCleanupService(it) }
}

var lastDrop = 0
    private set

fun cleanDrop(announce: Boolean = true, onComplete: ((Int) -> Unit)? = null) {
    if (!dropCfg.enabled) {
        RuntimeServices.messages.debug { "Drop cleanup is disabled" }
        onComplete?.invoke(0)
        return
    }
    val service = resolveDropService() ?: run {
        onComplete?.invoke(0)
        return
    }
    RuntimeServices.messages.debug { "Starting drop cleanup" }
    RuntimeServices.messages.debug { if (dropCfg.protectEnchanted) "Protect enchanted items" else "Remove enchanted items" }
    RuntimeServices.messages.debug { if (dropCfg.protectWrittenBook) "Protect written books" else "Remove written books" }

    val time = System.currentTimeMillis()
    service.cleanAllWorlds { results ->
        val elapsed = System.currentTimeMillis() - time
        lastDrop = results.sumOf { it.cleaned }
        RuntimeServices.statusSnapshots.updateCleanup { it.copy(lastDrop = lastDrop) }
        RuntimeServices.messages.debug { "Drop cleanup finished: ${lastDrop} removed, ${elapsed}ms" }
        if (announce) announceDrop(results)
        onComplete?.invoke(lastDrop)
    }
}

private fun announceDrop(results: List<DropCleanupResult>) {
    val all = results.sumOf { it.total }
    val finish = dropCfg.finishMessage
    if (finish.isBlank()) return
    val message = finish.placeholder("clean" to lastDrop, "all" to all)
    if (noOnline && !noOnlineMessage) return
    RuntimeServices.cleanupAnnouncementService.announceDropFinish(message)
}
