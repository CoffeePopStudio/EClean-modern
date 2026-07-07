package top.e404.eclean.clean

import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config
import top.e404.eclean.feature.cleanup.chunk.ChunkAlertService
import top.e404.eclean.feature.cleanup.chunk.ChunkDensityScanner
import top.e404.eclean.util.info
import top.e404.eclean.util.noOnline
import top.e404.eclean.util.noOnlineMessage
import top.e404.eplugin.EPlugin.Companion.placeholder

private inline val chunkCfg get() = Config.config.chunk
private val chunkScanner by lazy { ChunkDensityScanner() }
private val chunkAlertService by lazy { ChunkAlertService() }

/**
 * 最近一次清理区块密集实体的数量
 */
var lastChunk = 0
    private set

/**
 * 清理全服区块中的密集实体
 */
fun cleanDenseEntities(announce: Boolean = true) {
    if (!chunkCfg.enable) {
        PL.debug { "密集实体清理已禁用" }
        return
    }
    val worlds = Bukkit.getWorlds().filterNot { chunkCfg.disableWorld.any { regex -> it.name matches regex } }
    PL.debug { "开始进行密集实体检查" }
    PL.debug {
        buildString {
            append("启用密集实体检查的世界: [")
            worlds.joinTo(this, ", ", transform = World::getName)
            append("]")
        }
    }
    PL.debug { if (chunkCfg.settings.name) "清理被命名的生物" else "不清理被命名的生物" }
    PL.debug { if (chunkCfg.settings.lead) "清理拴绳拴住的生物" else "不清理拴绳拴住的生物" }
    PL.debug { if (chunkCfg.settings.mount) "清理乘骑中的生物" else "不清理乘骑中的生物" }

    var time = System.currentTimeMillis()
    val result = chunkScanner.cleanAllWorlds()
    lastChunk = result.cleaned
    time = System.currentTimeMillis() - time
    RuntimeServices.statusSnapshots.updateCleanup { it.copy(lastChunk = lastChunk) }

    PL.debug { "密集实体清理共${lastChunk}个, 耗时${time}ms" }
    chunkAlertService.alert(result.denseEntries)
    if (!announce) return

    if (noOnline) {
        if (noOnlineMessage) {
            val finish = Config.config.chunk.finish
            if (finish.isNotBlank()) RuntimeServices.cleanupAnnouncementService.announceChunkFinish(
                finish.placeholder("clean" to lastChunk)
            )
        }
    } else {
        val finish = Config.config.chunk.finish
        if (finish.isNotBlank()) RuntimeServices.cleanupAnnouncementService.announceChunkFinish(
            finish.placeholder("clean" to lastChunk)
        )
    }
}

/**
 * 清理指定世界的密集实体
 *
 * @return 清理的实体数量
 */
fun World.cleanChunkDenseEntities() = chunkScanner.cleanWorld(this)

private fun Chunk.cleanDenseEntities(): Int {
    return chunkScanner.cleanWorld(world)
}

fun Chunk.info() = "x: ${x * 16}..${x * 16 + 15}, z: ${z * 16}..${z * 16 + 15}"
