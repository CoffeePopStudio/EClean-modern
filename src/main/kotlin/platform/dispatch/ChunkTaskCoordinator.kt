package top.e404.eclean.platform.dispatch

import org.bukkit.Location
import org.bukkit.World
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.platform.execution.ChunkRef
import java.util.concurrent.atomic.AtomicInteger

class ChunkTaskCoordinator {
    fun dispatchToChunks(
        chunkRefs: List<ChunkRef>,
        resolveWorld: (String) -> World?,
        perChunk: (World, ChunkRef) -> Unit,
        onComplete: () -> Unit,
    ) {
        if (chunkRefs.isEmpty()) {
            Schedulers.runGlobal(onComplete)
            return
        }
        val pending = AtomicInteger(chunkRefs.size)
        chunkRefs.forEach { ref ->
            val world = resolveWorld(ref.world)
            if (world == null) {
                if (pending.decrementAndGet() == 0) Schedulers.runGlobal(onComplete)
                return@forEach
            }
            val loc = Location(world, ref.x * 16.0 + 8.0, 64.0, ref.z * 16.0 + 8.0)
            Schedulers.runAtLocation(loc) {
                try {
                    perChunk(world, ref)
                } finally {
                    if (pending.decrementAndGet() == 0) Schedulers.runGlobal(onComplete)
                }
            }
        }
    }

    fun getLoadedChunkRefs(world: World): List<ChunkRef> {
        val name = world.name
        return world.loadedChunks.map { ChunkRef(name, it.x, it.z) }
    }
}
