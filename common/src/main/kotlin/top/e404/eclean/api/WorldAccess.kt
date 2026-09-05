package top.e404.eclean.common.api

import top.e404.eclean.platform.execution.ChunkRef

/**
 * Platform-agnostic access to loaded worlds and chunks.
 */
interface WorldAccess {
    fun worldNames(): List<String>
    fun getLoadedChunkRefs(worldName: String): List<top.e404.eclean.platform.execution.ChunkRef>
    fun getChunk(worldName: String, ref: top.e404.eclean.platform.execution.ChunkRef): top.e404.eclean.common.api.CommonChunk?
}
