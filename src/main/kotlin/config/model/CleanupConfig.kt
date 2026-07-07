package top.e404.eclean.config.model

import kotlinx.serialization.Serializable

@Serializable
data class CleanupConfig(
    val intervalSeconds: Long = 600,
    val countdownMessages: Map<Long, String> = mapOf(
        60L to "&f将在1分钟后进行清理",
        30L to "&f将在30秒后进行清理",
        10L to "&f将在10秒后进行清理",
        0L to "&f正在清理",
    ),
    val cleanWhenNoPlayers: Boolean = true,
    val broadcastWhenNoPlayers: Boolean = true,
)
