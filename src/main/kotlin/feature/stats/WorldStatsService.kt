package top.e404.eclean.feature.stats

import org.bukkit.Bukkit
import org.bukkit.Location
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
                        WorldStatsResult(emptyMap(), liveChunks.size, forceLoaded)
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
        onComplete: (List<ChunkEntityCount>) -> Unit,
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
        val entries = mutableListOf<ChunkEntityCount>()
        coordinator.dispatchToChunks(
            chunkRefs = chunkRefs,
            resolveWorld = { Bukkit.getWorld(it) },
            perChunk = { w, ref ->
                val chunk = w.getChunkAt(ref.x, ref.z)
                val count = collector.countEntityTypeInChunk(chunk, type)
                if (count > minCount) {
                    synchronized(entries) { entries += ChunkEntityCount(ref.x, ref.z, count) }
                }
            },
            onComplete = {
                onComplete(entries.sortedByDescending { it.count })
            },
        )
    }

    fun collectChunkEntities(
        worldName: String,
        type: EntityType,
        chunkX: Int,
        chunkZ: Int,
        onComplete: (List<EntityLocationDetail>) -> Unit,
    ) {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            Schedulers.runGlobal { onComplete(emptyList()) }
            return
        }
        val location = Location(world, chunkX * 16.0 + 8.0, 64.0, chunkZ * 16.0 + 8.0)
        Schedulers.runAtLocation(location) {
            val chunk = world.getChunkAt(chunkX, chunkZ)
            val entities = chunk.entities.filter { it.type == type }
            onComplete(entities.map { EntityLocationDetail(it.location.x, it.location.y, it.location.z) })
        }
    }
}
