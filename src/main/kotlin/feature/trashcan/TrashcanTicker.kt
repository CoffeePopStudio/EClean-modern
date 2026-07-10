package top.e404.eclean.feature.trashcan

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import top.e404.eclean.config.Config
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.service.StatusSnapshotService

class TrashcanTicker(
    private val manager: TrashcanManager,
    private val snapshots: StatusSnapshotService,
) {
    private var task: ScheduledTask? = null

    var countdown: Long = 0
        private set

    fun start() {
        stop()
        val trashcanConfig = Config.current.trashcan
        val duration = trashcanConfig.clearIntervalSeconds ?: run {
            updateCountdown(0)
            return
        }
        if (!trashcanConfig.enabled) {
            updateCountdown(0)
            return
        }
        updateCountdown(duration)
        task = Schedulers.scheduleRepeatingGlobal(20, 20) {
            val next = (countdown - 1).coerceAtLeast(0)
            updateCountdown(next)
            if (next <= 0L) {
                updateCountdown(duration)
                manager.clearAll()
            }
        }
    }

    fun stop() {
        task?.cancel()
        task = null
    }

    fun restart() {
        stop()
        start()
    }

    private fun updateCountdown(value: Long) {
        countdown = value
        snapshots.updateTrashcanCountdown(value)
    }
}
