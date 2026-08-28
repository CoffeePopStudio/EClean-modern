package top.e404.eclean.feature.cleanup.drop

import top.e404.eclean.config.Config
import top.e404.eclean.config.planEnabledWorlds

class DropCleanupPlanner {
    fun planWorldNames(): List<String> =
        planEnabledWorlds(Config.current.drop.disabledWorlds)
}
