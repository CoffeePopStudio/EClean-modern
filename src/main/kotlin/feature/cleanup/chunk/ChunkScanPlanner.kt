package top.e404.eclean.feature.cleanup.chunk

import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import top.e404.eclean.config.Config

class ChunkScanPlanner {
    fun planWorlds(includeDisabled: Boolean = false): List<World> {
        val worlds = Bukkit.getWorlds()
        if (includeDisabled) return worlds
        val disabledWorlds = Config.current.chunkDensity.disabledWorlds
        return worlds.filterNot { world ->
            disabledWorlds.any { regex -> world.name matches regex }
        }
    }

    fun planChunks(world: World): List<Chunk> = world.loadedChunks.toList()
}
