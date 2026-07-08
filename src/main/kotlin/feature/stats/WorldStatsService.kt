package top.e404.eclean.feature.stats

import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.platform.dispatch.ChunkTaskCoordinator

class WorldStatsService(
    private val coordinator: ChunkTaskCoordinator = ChunkTaskCoordinator(),
    private val collector: WorldStatsCollector = WorldStatsCollector(),
) {
    fun collectWorldStats(worldName: String, onComplete: (WorldStatsResult?) -> Unit) {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            Schedulers.runGlobal { onComplete(null) }
            return
        }
        val chunkRefs = coordinator.getLoadedChunkRefs(world)
        if (chunkRefs.isEmpty()) {
            Schedulers.runGlobal { onComplete(WorldStatsResult(emptyMap(), 0, 0)) }
            return
        }
        val snapshots = mutableListOf<ChunkSnapshot>()
        val liveChunks = mutableListOf<org.bukkit.Chunk>()
        coordinator.dispatchToChunks(
            chunkRefs = chunkRefs,
            resolveWorld = { Bukkit.getWorld(it) },
            perChunk = { w, ref ->
                val chunk = w.getChunkAt(ref.x, ref.z)
                synchronized(liveChunks) { liveChunks += chunk }
                val snap = collector.collectFromChunk(chunk)
                if (snap.entityCounts.isNotEmpty()) {
                    synchronized(snapshots) { snapshots += snap }
                }
            },
            onComplete = {
                Schedulers.runGlobal {
                    val forceLoaded = liveChunks.count { it.isForceLoaded }
                    val result = if (snapshots.isEmpty()) {
                        val empty = ChunkSnapshot(emptyMap(), false)
                        ChunkSnapshot.merge(listOf(empty).repeat(liveChunks.size), forceLoaded)
                    } else {
                        ChunkSnapshot.merge(snapshots, forceLoaded)
                    }
                    onComplete(result)
                }
            },
        )
    }

    fun collectEntityStats(
        worldName: String,
        type: EntityType,
        minCount: Int,
        onComplete: (List<Pair<String, Int>>) -> Unit,
    ) {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            Schedulers.runGlobal { onComplete(emptyList()) }
            return
        }
        val chunkRefs = coordinator.getLoadedChunkRefs(world)
        if (chunkRefs.isEmpty()) {
            Schedulers.runGlobal { onComplete(emptyList()) }
            return
        }
        val entries = mutableListOf<Pair<String, Int>>()
        coordinator.dispatchToChunks(
            chunkRefs = chunkRefs,
            resolveWorld = { Bukkit.getWorld(it) },
            perChunk = { w, ref ->
                val chunk = w.getChunkAt(ref.x, ref.z)
                val count = collector.countEntityTypeInChunk(chunk, type)
                if (count > minCount) {
                    val label = "x: ${chunk.x * 16}..${chunk.x * 16 + 15}, z: ${chunk.z * 16}..${chunk.z * 16 + 15}"
                    synchronized(entries) { entries += label to count }
                }
            },
            onComplete = {
                onComplete(entries.sortedByDescending { it.second })
            },
        )
    }
}

private fun <T> List<T>.repeat(n: Int): List<T> {
    val result = mutableListOf<T>()
    repeat(n) { result += this }
    return result
}
