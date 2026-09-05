package top.e404.eclean.feature.cleanup.living

import top.e404.eclean.PL
import top.e404.eclean.config.Config
import top.e404.eclean.config.planEnabledWorlds

class LivingCleanupPlanner {
    fun planWorldNames(): List<String> =
        planEnabledWorlds(
            worldNames = PL.services.commonPlatform.serverInfo.worldNames,
            disabledWorlds = Config.current.living.disabledWorlds,
            perWorld = Config.current.perWorld.worlds,
        )
}
