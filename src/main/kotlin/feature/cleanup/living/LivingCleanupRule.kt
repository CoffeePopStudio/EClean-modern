package top.e404.eclean.feature.cleanup.living

data class LivingCleanupRule(
    val cleanNamed: Boolean,
    val cleanLeashed: Boolean,
    val cleanMounted: Boolean,
    val blackList: Boolean,
) {
    companion object {
        fun fromConfig() = top.e404.eclean.config.Config.current.living.let { cfg ->
            LivingCleanupRule(
                cleanNamed = cfg.settings.cleanNamed,
                cleanLeashed = cfg.settings.cleanLeashed,
                cleanMounted = cfg.settings.cleanMounted,
                blackList = cfg.blacklistMode,
            )
        }
    }
}
