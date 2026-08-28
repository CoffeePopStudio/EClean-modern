package top.e404.eclean.feature.cleanup.chunk

import org.bukkit.Bukkit
import org.bukkit.World
import top.e404.eclean.config.Config
import top.e404.eclean.config.planEnabledWorlds
import top.e404.eclean.platform.execution.ChunkRef

class ChunkScanPlanner {
    fun planWorldNames(includeDisabled: Boolean = false): List<String> {
        if (includeDisabled) return Bukkit.getWorlds().map { it.name }
        return planEnabledWorlds(Config.current.chunkDensity.disabledWorlds)
    }

    fun planChunks(world: World): List<ChunkRef> =
        world.loadedChunks.map { ChunkRef(world.name, it.x, it.z) }
}
