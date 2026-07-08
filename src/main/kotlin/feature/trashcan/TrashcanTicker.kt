package top.e404.eclean.feature.trashcan

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import top.e404.eclean.config.Config
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.service.StatusSnapshotService

class TrashcanTicker(
    private val service: TrashcanService,
    private val snapshots: StatusSnapshotService,
) {
    private var task: ScheduledTask? = null

    fun start() {
        stop()
        val trashcanConfig = Config.current.trashcan
        val duration = trashcanConfig.clearIntervalSeconds ?: run {
            service.syncCountdown(0)
            return
        }
        if (!trashcanConfig.enabled) return
        service.syncCountdown(duration)
        task = Schedulers.scheduleRepeatingGlobal(20, 20) {
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
