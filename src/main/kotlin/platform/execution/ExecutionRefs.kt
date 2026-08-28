package top.e404.eclean.platform.execution

import java.util.UUID

data class ChunkRef(
    val world: String,
    val x: Int,
    val z: Int,
)

data class EntityRef(
    val uuid: UUID,
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
)

data class PlayerRef(
    val uuid: UUID,
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
)
