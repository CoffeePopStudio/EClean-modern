package top.e404.eclean.feature.cleanup.drop

data class DropCleanupRule(
    val blackList: Boolean,
    val protectEnchanted: Boolean,
    val protectLore: Boolean,
    val protectWrittenBook: Boolean,
) {
    companion object {
        fun fromConfig() = top.e404.eclean.config.Config.current.drop.let { cfg ->
            DropCleanupRule(
                blackList = cfg.blacklistMode,
                protectEnchanted = cfg.protectEnchanted,
                protectLore = cfg.protectLore,
                protectWrittenBook = cfg.protectWrittenBook,
            )
        }
    }
}
