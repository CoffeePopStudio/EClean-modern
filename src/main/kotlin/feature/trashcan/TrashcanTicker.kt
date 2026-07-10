package top.e404.eclean.feature.trashcan

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import top.e404.eclean.app.MessageService
import top.e404.eclean.config.Config
import top.e404.eclean.lang.MLang
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.service.StatusSnapshotService

class TrashcanTicker(
    private val manager: TrashcanManager,
    private val snapshots: StatusSnapshotService,
    private val messages: MessageService,
) {
    private var task: ScheduledTask? = null
    private var lastIntervalSeconds: Long? = null

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
        if (countdown <= 0 || duration != lastIntervalSeconds) {
            updateCountdown(duration)
        }
        lastIntervalSeconds = duration
        task = Schedulers.scheduleRepeatingGlobal(20, 20) {
            val next = (countdown - 1).coerceAtLeast(0)
            updateCountdown(next)
            if (next in reminderSeconds) {
                val reminder = MLang["command.trash_clean_reminder", "seconds" to next.toString()]
                for (player in Bukkit.getOnlinePlayers()) {
                    messages.send(player, reminder)
                }
            }
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

    companion object {
        private val reminderSeconds = setOf(60L, 30L, 10L)
    }
}
