package top.e404.eclean.config.model

import kotlinx.serialization.Serializable
import top.e404.eclean.config.serialization.RegexSerializer

@Serializable
data class EntityRuleSettings(
    val cleanNamed: Boolean = false,
    val cleanLeashed: Boolean = false,
    val cleanMounted: Boolean = false,
)

@Serializable
data class LivingConfig(
    val enabled: Boolean = true,
    val disabledWorlds: List<@Serializable(with = RegexSerializer::class) Regex> = emptyList(),
    val finishMessage: String = "",
    val settings: EntityRuleSettings = EntityRuleSettings(),
    val blacklistMode: Boolean = true,
    val matchers: List<@Serializable(with = RegexSerializer::class) Regex> = emptyList(),
)
