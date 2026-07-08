package top.e404.eclean.feature.cleanup.chunk

import org.bukkit.Bukkit
import org.bukkit.World
import top.e404.eclean.config.Config
import top.e404.eclean.platform.execution.ChunkRef

class ChunkScanPlanner {
    fun planWorldNames(includeDisabled: Boolean = false): List<String> {
        val worlds = Bukkit.getWorlds()
        if (includeDisabled) return worlds.map { it.name }
        val disabledWorlds = Config.current.chunkDensity.disabledWorlds
        return worlds
            .filterNot { world -> disabledWorlds.any { regex -> world.name matches regex } }
            .map { it.name }
    }

    fun planChunks(world: World): List<ChunkRef> =
        world.loadedChunks.map { ChunkRef(world.name, it.x, it.z) }
}
