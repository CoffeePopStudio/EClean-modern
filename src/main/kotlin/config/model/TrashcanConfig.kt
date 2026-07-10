package top.e404.eclean.config.model

import kotlinx.serialization.Serializable
import top.e404.eclean.config.serialization.RegexSerializer

@Serializable
data class DespawnRecoveryConfig(
    val enabled: Boolean = false,
    val disabledWorlds: List<@Serializable(with = RegexSerializer::class) Regex> = emptyList(),
    val matchers: List<@Serializable(with = RegexSerializer::class) Regex> = emptyList(),
)

@Serializable
data class TrashcanConfig(
    val enabled: Boolean = true,
    val collectFromDropCleanup: Boolean = true,
    val clearIntervalSeconds: Long? = 600,
    val maxSlots: Int = 54,
    val despawnRecovery: DespawnRecoveryConfig = DespawnRecoveryConfig(),
)
