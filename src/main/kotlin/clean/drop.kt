package top.e404.eclean.clean

import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.ModernConfig
import top.e404.eclean.feature.cleanup.drop.DropCleanupResult
import top.e404.eclean.feature.cleanup.drop.DropCleanupService
import top.e404.eclean.util.noOnline
import top.e404.eclean.util.noOnlineMessage
import top.e404.eplugin.EPlugin.Companion.placeholder

private inline val dropCfg get() = ModernConfig.drop

private fun resolveDropService(): DropCleanupService? {
    val scheduler = if (RuntimeServices.isSchedulerReady) RuntimeServices.scheduler else null
    return scheduler?.let { DropCleanupService(it) }
}

var lastDrop = 0
    private set

fun cleanDrop(announce: Boolean = true, onComplete: ((Int) -> Unit)? = null) {
    if (!dropCfg.enabled) {
        PL.debug { "掉落物清理已禁用" }
        onComplete?.invoke(0)
        return
    }
    val service = resolveDropService() ?: run {
        onComplete?.invoke(0)
        return
    }
    PL.debug { "开始清理掉落物" }
    PL.debug { if (dropCfg.protectEnchanted) "不清理附魔的物品" else "清理附魔的物品" }
    PL.debug { if (dropCfg.protectWrittenBook) "不清理成书" else "清理成书" }

    val time = System.currentTimeMillis()
    service.cleanAllWorlds { results ->
        val elapsed = System.currentTimeMillis() - time
        lastDrop = results.sumOf { it.cleaned }
        RuntimeServices.statusSnapshots.updateCleanup { it.copy(lastDrop = lastDrop) }
        PL.debug { "掉落物清理共${lastDrop}个, 耗时${elapsed}ms" }
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
