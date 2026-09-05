package top.e404.eclean.feature.cleanup.drop

import top.e404.eclean.PL
import top.e404.eclean.config.Config

class DropCleanupService {
    private val engine = DropCleanupEngine(
        worldAccess = PL.services.commonPlatform.worldAccess,
        scheduler = PL.services.commonPlatform.scheduler,
    )

    fun cleanAllWorlds(
        dryRun: Boolean = false,
        onComplete: (List<DropCleanupResult>) -> Unit,
    ) {
        engine.cleanAllWorlds(Config.current, dryRun, onComplete)
    }

    fun cleanWorld(
        worldName: String,
        dryRun: Boolean = false,
        onComplete: (DropCleanupResult) -> Unit,
    ) {
        engine.cleanWorld(worldName, Config.current, dryRun, onComplete)
    }
}
