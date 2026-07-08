package top.e404.eclean.feature.cleanup

import top.e404.eclean.clean.cleanDenseEntities
import top.e404.eclean.clean.cleanDrop
import top.e404.eclean.clean.cleanLiving
import top.e404.eclean.config.Config
import top.e404.eclean.service.StatusSnapshotService
import top.e404.eclean.app.MessageService

class CleanupCoordinator(
    private val messages: MessageService,
    private val snapshots: StatusSnapshotService,
) {
    fun cleanNow(onComplete: (() -> Unit)? = null) {
        messages.debug { "通过 CleanupCoordinator 触发一次完整清理" }
        cleanDrop(announce = true) {
            cleanLiving(announce = true) {
                cleanDenseEntities(announce = true) {
                    snapshots.updateCleanup {
                        it.copy(
                            elapsedSeconds = 0,
                            remainingSeconds = Config.current.cleanup.intervalSeconds,
                        )
                    }
                    onComplete?.invoke()
                }
            }
        }
    }
}
