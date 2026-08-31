package top.e404.eclean.feature.cleanup.living

import top.e404.eclean.config.model.LivingTypeRule

data class LivingCleanupRule(
    val cleanNamed: Boolean,
    val cleanLeashed: Boolean,
    val cleanMounted: Boolean,
    val blackList: Boolean,
    val maxDistance: Double?,
    val typeRules: Map<String, LivingTypeRule>,
    val protectTamed: Boolean = true,
    val protectAllay: Boolean = true,
) {
    companion object {
        fun fromConfig(worldName: String? = null) = top.e404.eclean.config.Config.current.let { config ->
            val cfg = config.living
            val perWorld = worldName?.let { config.perWorld.worlds[it] }
            LivingCleanupRule(
                cleanNamed = cfg.settings.cleanNamed,
                cleanLeashed = cfg.settings.cleanLeashed,
                cleanMounted = cfg.settings.cleanMounted,
                blackList = cfg.blacklistMode,
                maxDistance = perWorld?.livingMaxDistance ?: cfg.maxDistance,
                typeRules = cfg.typeRules,
                protectTamed = cfg.protectTamed,
                protectAllay = cfg.protectAllay,
            )
        }
    }
}
