package top.e404.eclean.config.model

import kotlinx.serialization.Serializable

/** Which configuration preset is active. */
enum class ConfigProfile(val id: String) {
    NORMAL("normal"),
    DEV("dev");

    companion object {
        fun fromId(value: String?): ConfigProfile =
            entries.firstOrNull { it.id.equals(value, ignoreCase = true) } ?: NORMAL
    }
}

@Serializable
data class ProfileConfig(
    val profile: String = ConfigProfile.NORMAL.id,
)
