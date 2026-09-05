package top.e404.eclean.feature.cleanup.drop

import top.e404.eclean.PL
import top.e404.eclean.config.Config
import top.e404.eclean.config.planEnabledWorlds

class DropCleanupPlanner {
    fun planWorldNames(): List<String> =
        planEnabledWorlds(
            worldNames = PL.services.commonPlatform.serverInfo.worldNames,
            disabledWorlds = Config.current.drop.disabledWorlds,
            perWorld = Config.current.perWorld.worlds,
        )
}
