package top.e404.eclean.config.model

import kotlinx.serialization.Serializable

@Serializable
data class GlobalConfig(
    val debug: Boolean = false,
    val updateCheck: Boolean = true,
    val language: String = "zh_cn",
)
