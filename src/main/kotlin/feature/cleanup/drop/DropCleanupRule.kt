package top.e404.eclean.feature.cleanup.drop

data class DropCleanupRule(
    val blackList: Boolean,
    val protectEnchanted: Boolean,
    val protectLore: Boolean,
    val protectWrittenBook: Boolean,
)
