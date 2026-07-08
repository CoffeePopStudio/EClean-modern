package top.e404.eclean.clean

import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.ModernConfig
import top.e404.eclean.feature.cleanup.chunk.ChunkAlertService
import top.e404.eclean.feature.cleanup.chunk.ChunkDensityScanner
import top.e404.eclean.platform.execution.ChunkRef
import top.e404.eclean.util.noOnline
import top.e404.eclean.util.noOnlineMessage
import top.e404.eclean.util.placeholder

private inline val chunkCfg get() = ModernConfig.chunkDensity

private fun resolveChunkScanner(): ChunkDensityScanner? {
    val scheduler = if (RuntimeServices.isSchedulerReady) RuntimeServices.scheduler else null
    return scheduler?.let { ChunkDensityScanner(it) }
}

private val chunkAlertService by lazy { ChunkAlertService() }

var lastChunk = 0
    private set

fun cleanDenseEntities(announce: Boolean = true, onComplete: ((Int) -> Unit)? = null) {
    if (!chunkCfg.enabled) {
        RuntimeServices.messages.debug { "密集实体清理已禁用" }
        onComplete?.invoke(0)
        return
    }
    val scanner = resolveChunkScanner() ?: run {
        onComplete?.invoke(0)
        return
    }
    RuntimeServices.messages.debug { "开始进行密集实体检查" }
    RuntimeServices.messages.debug { if (chunkCfg.settings.cleanNamed) "清理被命名的生物" else "不清理被命名的生物" }
    RuntimeServices.messages.debug { if (chunkCfg.settings.cleanLeashed) "清理拴绳拴住的生物" else "不清理拴绳拴住的生物" }
    RuntimeServices.messages.debug { if (chunkCfg.settings.cleanMounted) "清理乘骑中的生物" else "不清理乘骑中的生物" }

    val time = System.currentTimeMillis()
    scanner.cleanAllWorlds { result ->
        val elapsed = System.currentTimeMillis() - time
        lastChunk = result.cleaned
        RuntimeServices.statusSnapshots.updateCleanup { it.copy(lastChunk = lastChunk) }
        RuntimeServices.messages.debug { "密集实体清理共${lastChunk}个, 耗时${elapsed}ms" }
        chunkAlertService.alert(result.denseEntries)
        if (announce) announceChunk()
        onComplete?.invoke(lastChunk)
    }
}

fun scanDenseEntries(onComplete: (List<top.e404.eclean.feature.cleanup.chunk.ChunkDensityEntry>) -> Unit) {
    val scanner = resolveChunkScanner()
    if (scanner == null) { onComplete(emptyList()); return }
    scanner.scanDenseEntries(onComplete)
}

private fun announceChunk() {
    val finish = chunkCfg.finishMessage
    if (finish.isBlank()) return
    val message = finish.placeholder("clean" to lastChunk)
    if (noOnline && !noOnlineMessage) return
    RuntimeServices.cleanupAnnouncementService.announceChunkFinish(message)
}

fun ChunkRef.info() = "x: ${x * 16}..${x * 16 + 15}, z: ${z * 16}..${z * 16 + 15}"
