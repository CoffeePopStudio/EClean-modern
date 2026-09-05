package top.e404.eclean.clean
import top.e404.eclean.PL
import top.e404.eclean.config.Config
import top.e404.eclean.config.ModernConfig
import top.e404.eclean.feature.cleanup.chunk.ChunkAlertService
import top.e404.eclean.feature.cleanup.chunk.ChunkDensityScanner
import top.e404.eclean.lang.MLang
import top.e404.eclean.platform.execution.ChunkRef
import top.e404.eclean.util.noOnline
import top.e404.eclean.util.noOnlineMessage
import top.e404.eclean.util.placeholder

private inline val chunkCfg get() = ModernConfig.chunkDensity

private fun resolveChunkScanner(): ChunkDensityScanner = ChunkDensityScanner()

private val chunkAlertService by lazy {
    ChunkAlertService(
        messageSender = PL.services.commonPlatform.messageSender,
        serverInfo = PL.services.commonPlatform.serverInfo,
        permissionService = PL.services.commonPlatform.permissionService,
        prefixProvider = { MLang["prefix"] },
        alertFormatProvider = { Config.current.chunkDensity.alertFormat },
    )
}

var lastChunk = 0
    private set

fun cleanDenseEntities(announce: Boolean = true, dryRun: Boolean = false, onComplete: ((Int) -> Unit)? = null) {
    if (!chunkCfg.enabled) {
        PL.services.messages.debug { "Chunk density cleanup is disabled" }
        onComplete?.invoke(0)
        return
    }
    val scanner = resolveChunkScanner()
    PL.services.messages.debug { "Starting chunk density check" }
    PL.services.messages.debug { if (chunkCfg.settings.cleanNamed) "Clean named entities" else "Skip named entities" }
    PL.services.messages.debug { if (chunkCfg.settings.cleanLeashed) "Clean leashed entities" else "Skip leashed entities" }
    PL.services.messages.debug { if (chunkCfg.settings.cleanMounted) "Clean mounted entities" else "Skip mounted entities" }

    val time = System.currentTimeMillis()
    scanner.cleanAllWorlds(dryRun = dryRun) { result ->
        val elapsed = System.currentTimeMillis() - time
        lastChunk = result.cleaned
        PL.services.statusSnapshots.updateCleanup { it.copy(lastChunk = lastChunk) }
        PL.services.messages.debug { "Chunk density cleanup finished: ${lastChunk} removed, ${elapsed}ms" }
        chunkAlertService.alert(result.denseEntries)
        if (announce) announceChunk()
        onComplete?.invoke(lastChunk)
    }
}

fun scanDenseEntries(onComplete: (List<top.e404.eclean.feature.cleanup.chunk.ChunkDensityEntry>) -> Unit) {
    val scanner = resolveChunkScanner()
    scanner.scanDenseEntries(onComplete)
}

private fun announceChunk() {
    val finish = chunkCfg.finishMessage
    if (finish.isBlank()) return
    val message = finish.placeholder("clean" to lastChunk)
    if (noOnline && !noOnlineMessage) return
    PL.services.cleanupAnnouncementService.announceFinish(message)
}
