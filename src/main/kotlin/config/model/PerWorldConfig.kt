package top.e404.eclean.config.model

import kotlinx.serialization.Serializable

@Serializable
data class PerWorldEntry(
    val enabled: Boolean? = null,
    val intervalSeconds: Long? = null,
)

@Serializable
data class PerWorldConfig(
    val worlds: Map<String, PerWorldEntry> = emptyMap(),
)
