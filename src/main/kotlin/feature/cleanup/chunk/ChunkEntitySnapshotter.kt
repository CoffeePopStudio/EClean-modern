package top.e404.eclean.feature.cleanup.chunk

import org.bukkit.Chunk
import top.e404.eclean.platform.execution.ChunkRef
import top.e404.eclean.platform.snapshot.ChunkEntitySnapshot
import top.e404.eclean.platform.snapshot.ChunkEntityState

class ChunkEntitySnapshotter {
    fun snapshot(chunk: Chunk): ChunkEntitySnapshot {
        val entities = chunk.entities.map { entity ->
            ChunkEntityState(
                uuid = entity.uniqueId,
                type = entity.type.name,
                named = entity.customName != null,
                leashed = entity is org.bukkit.entity.LivingEntity && entity.isLeashed,
                mounted = entity.isInsideVehicle || entity.passengers.isNotEmpty(),
            )
        }
        return ChunkEntitySnapshot(
            chunk = ChunkRef(chunk.world.name, chunk.x, chunk.z),
            entities = entities,
        )
    }
}
