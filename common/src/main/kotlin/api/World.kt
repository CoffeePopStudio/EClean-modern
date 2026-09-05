package top.e404.eclean.common.api

import top.e404.eclean.platform.execution.ChunkRef
import java.util.UUID

/**
 * Loader-agnostic world abstraction. Paper/Fabric/NeoForge implementations
 * adapt their native world/chunk/entity objects to these interfaces.
 */
interface CommonWorld {
    val name: String
    fun getLoadedChunkRefs(): List<ChunkRef>
    fun getChunk(ref: ChunkRef): CommonChunk?
}

interface CommonChunk {
    val ref: ChunkRef
    fun entities(): List<CommonEntity>
    fun items(): List<CommonItem>
    fun livingEntities(): List<CommonLivingEntity>
}

interface CommonEntity {
    val uniqueId: UUID
    val type: String
    val location: CommonLocation
    fun remove()
}

interface CommonItem : CommonEntity {
    val enchanted: Boolean
    val hasLore: Boolean
    val isWrittenBook: Boolean
    val distanceToNearestPlayer: Double?
}

interface CommonLivingEntity : CommonEntity {
    val named: Boolean
    val leashed: Boolean
    val mounted: Boolean
    val tamed: Boolean
    val allay: Boolean
    val distanceToNearestPlayer: Double?
}
