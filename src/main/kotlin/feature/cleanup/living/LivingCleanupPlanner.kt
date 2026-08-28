package top.e404.eclean.feature.cleanup.living

import top.e404.eclean.config.Config
import top.e404.eclean.config.planEnabledWorlds

class LivingCleanupPlanner {
    fun planWorldNames(): List<String> =
        planEnabledWorlds(Config.current.living.disabledWorlds)
}
