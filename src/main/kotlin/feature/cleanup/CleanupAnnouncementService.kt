package top.e404.eclean.feature.cleanup

import top.e404.eclean.config.Config
import top.e404.eclean.util.noOnline
import top.e404.eclean.util.noOnlineMessage
import top.e404.eclean.app.MessageService

class CleanupAnnouncementService(
    private val messages: MessageService,
    private val snapshots: top.e404.eclean.service.StatusSnapshotService,
) {
    fun announceCountdown(remainingSeconds: Long) {
        val message = Config.current.cleanup.countdownMessages[remainingSeconds] ?: return
        if (noOnline && !noOnlineMessage) return
        messages.broadcast(message)
    }

    fun announceDropFinish(message: String) {
        if (noOnline && !noOnlineMessage) return
        if (message.isNotBlank()) messages.broadcast(message)
    }

    fun announceLivingFinish(message: String) {
        if (noOnline && !noOnlineMessage) return
        if (message.isNotBlank()) messages.broadcast(message)
    }

    fun announceChunkFinish(message: String) {
        if (noOnline && !noOnlineMessage) return
        if (message.isNotBlank()) messages.broadcast(message)
    }
}
