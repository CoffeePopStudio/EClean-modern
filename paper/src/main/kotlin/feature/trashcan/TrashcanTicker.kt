package top.e404.eclean.feature.trashcan

import top.e404.eclean.common.api.ScheduledTask
import top.e404.eclean.config.Config
import top.e404.eclean.menu.MenuManager
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.service.StatusSnapshotService

/**
 * 垃圾桶逐条目过期器: 每秒扫描一次, 移除已到期的条目并刷新打开的菜单
 */
class TrashcanTicker(
    private val store: TrashcanItemStore,
    private val snapshots: StatusSnapshotService,
) {
    private var task: ScheduledTask? = null
    private var tick = 0

    /** 最早到期条目的剩余秒数, 没有条目或全部永不过期时为 0 */
    var countdown: Long = 0
        private set

    fun start() {
        stop()
        val trashcanConfig = Config.current.trashcan
        val duration = trashcanConfig.clearIntervalSeconds
        if (!trashcanConfig.enabled || duration == null) {
            updateCountdown(0)
            MenuManager.refreshTrashcanMenus()
            return
        }
        task = Schedulers.scheduleRepeatingGlobal(20, 20) {
            tick++
            val now = System.currentTimeMillis()
            val expired = store.expireEntries(now)
            val showRemaining = Config.current.trashcan.stacking.showRemainingTimeInLore
            if (expired > 0 || (showRemaining && tick % 5 == 0)) {
                MenuManager.refreshTrashcanMenus()
            }
            val remaining = store.earliestDeadline()?.let { maxOf(0, (it - now) / 1000) } ?: 0
            updateCountdown(remaining)
        }
        // Also refresh already-open menus when the trashcan config changes.
        MenuManager.refreshTrashcanMenus()
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
