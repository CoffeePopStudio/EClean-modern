package top.e404.eclean.clean

import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.ModernConfig
import top.e404.eclean.util.noOnline
import top.e404.eclean.util.noOnlineClean

object Clean {
    private val duration get() = ModernConfig.cleanupDuration

    /**
     * 计数, 每20tick++
     */
    val count get() = RuntimeServices.statusSnapshots.current().cleanup.elapsedSeconds

    fun schedule() {
        RuntimeServices.messages.info("&f设置清理任务, 间隔${duration}秒")
        RuntimeServices.cleanupTickService.start()
    }

    fun clean() {
        if (noOnline && !noOnlineClean) return
        RuntimeServices.cleanupCoordinator.cleanNow()
    }
}
