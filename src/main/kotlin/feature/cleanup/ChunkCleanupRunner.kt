package top.e404.eclean.feature.cleanup

import org.bukkit.Bukkit
import org.bukkit.World
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.platform.dispatch.ChunkTaskCoordinator
import top.e404.eclean.platform.execution.ChunkRef
import java.util.concurrent.atomic.AtomicInteger

data class ChunkCleanupOutcome(
    val cleaned: Int,
    val total: Int,
)

class ChunkCleanupRunner(
    private val coordinator: ChunkTaskCoordinator = ChunkTaskCoordinator(),
) {
    fun <R> cleanAllWorlds(
        worldNames: List<String>,
        onComplete: (List<R>) -> Unit,
        cleanWorld: (String, (R) -> Unit) -> Unit,
    ) {
        if (worldNames.isEmpty()) {
            Schedulers.runGlobal { onComplete(emptyList()) }
            return
        }
        val results = mutableListOf<R>()
        val pending = AtomicInteger(worldNames.size)
        worldNames.forEach { name ->
            cleanWorld(name) { result ->
                synchronized(results) { results += result }
                if (pending.decrementAndGet() == 0) onComplete(results.toList())
            }
        }
    }

    fun cleanWorld(
        worldName: String,
        onEmpty: () -> Unit,
        perChunk: (World, ChunkRef) -> ChunkCleanupOutcome,
        onComplete: (Int, Int) -> Unit,
    ) {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            Schedulers.runGlobal(onEmpty)
            return
        }
        val chunkRefs = coordinator.getLoadedChunkRefs(world)
        if (chunkRefs.isEmpty()) {
            Schedulers.runGlobal(onEmpty)
            return
        }
        val cleaned = AtomicInteger(0)
        val total = AtomicInteger(0)
        coordinator.dispatchToChunks(
            chunkRefs = chunkRefs,
            resolveWorld = { Bukkit.getWorld(it) },
            perChunk = { w, ref ->
                val outcome = perChunk(w, ref)
                cleaned.addAndGet(outcome.cleaned)
                total.addAndGet(outcome.total)
            },
            onComplete = {
                onComplete(cleaned.get(), total.get())
            },
        )
    }
}
