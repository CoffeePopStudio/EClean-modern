package top.e404.eclean.feature.trashcan

import top.e404.eclean.config.Config
import top.e404.eclean.platform.SchedulerFacade
import top.e404.eclean.platform.SchedulerHandle
import top.e404.eclean.service.StatusSnapshotService

class TrashcanTicker(
    private val scheduler: SchedulerFacade,
    private val service: TrashcanService,
    private val snapshots: StatusSnapshotService,
) {
    private var task: SchedulerHandle? = null

    fun start() {
        stop()
        val duration = Config.config.trashcan.duration ?: run {
            service.syncCountdown(0)
            return
        }
        if (!Config.config.trashcan.enable) return
        service.syncCountdown(duration)
        task = scheduler.scheduleRepeatingGlobal(20, 20) {
            val next = (service.countdown - 1).coerceAtLeast(0)
            service.syncCountdown(next)
            if (next <= 0L) {
                service.syncCountdown(duration)
                service.clearAll()
            }
        }
    }

    fun stop() {
        task?.cancel()
        task = null
    }
}
