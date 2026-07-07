package top.e404.eclean.feature.cleanup.chunk

import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import top.e404.eclean.PL
import top.e404.eclean.config.Config
import top.e404.eclean.util.info

class ChunkDensityScanner {
    fun cleanAllWorlds(): ChunkDensityResult {
        val cfg = Config.config.chunk
        val worlds = Bukkit.getWorlds().filterNot { world ->
            cfg.disableWorld.any { regex -> world.name matches regex }
        }

        var cleaned = 0
        val dense = mutableListOf<ChunkDensityEntry>()
        worlds.forEach { world ->
            world.loadedChunks.forEach { chunk ->
                val result = cleanChunk(chunk)
                cleaned += result.cleaned
                dense += result.denseEntries
            }
        }
        return ChunkDensityResult(cleaned = cleaned, denseEntries = dense)
    }

    fun cleanWorld(world: World): Int = world.loadedChunks.sumOf { cleanChunk(it).cleaned }

    fun scanDenseEntries(): List<ChunkDensityEntry> {
        val threshold = Config.config.chunk.count
        return Bukkit.getWorlds()
            .flatMap { it.loadedChunks.toList() }
            .flatMap { chunk ->
                chunk.entities.groupBy(Entity::getType)
                    .filter { (_, list) -> list.size > threshold }
                    .map { (type, list) -> ChunkDensityEntry(chunk, type, list.size) }
            }
            .sortedByDescending { it.amount }
    }

    private fun cleanChunk(chunk: Chunk): ChunkDensityResult {
        val cfg = Config.config.chunk
        val willBeRemoved = chunk.entities.toMutableList()
        if (willBeRemoved.isEmpty()) return ChunkDensityResult(cleaned = 0, denseEntries = emptyList())

        if (!cfg.settings.name) willBeRemoved.removeIf { it.customName != null }
        if (!cfg.settings.lead) willBeRemoved.removeIf { it is LivingEntity && it.isLeashed }
        if (!cfg.settings.mount) willBeRemoved.removeIf { it.isInsideVehicle || it.passengers.isNotEmpty() }

        var cleaned = 0
        cfg.limit.entries.mapNotNull { (regex, limit) ->
            val matches = willBeRemoved.filter { it.type.name.matches(regex) }.toMutableList()
            if (matches.size <= limit) return@mapNotNull null
            matches.subList(limit, matches.size).also {
                cleaned += it.size
                willBeRemoved.removeAll(it)
            }
        }.forEach { toRemove ->
            toRemove.forEach(Entity::remove)
        }

        val denseEntries = chunk.entities.asList().info()
            .filter { it.value > cfg.count }
            .mapNotNull { (name, amount) ->
                val type = runCatching { org.bukkit.entity.EntityType.valueOf(name) }.getOrNull() ?: return@mapNotNull null
                ChunkDensityEntry(chunk, type, amount)
            }
        PL.debug { "区块${chunk.x},${chunk.z}密集清理完成($cleaned)" }
        return ChunkDensityResult(cleaned = cleaned, denseEntries = denseEntries)
    }
}
