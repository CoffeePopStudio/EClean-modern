package top.e404.eclean.feature.cleanup.living

data class LivingCleanupRule(
    val cleanNamed: Boolean,
    val cleanLeashed: Boolean,
    val cleanMounted: Boolean,
    val blackList: Boolean,
)
