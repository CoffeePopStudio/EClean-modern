package top.e404.eclean.service

import top.e404.eclean.common.api.ScheduledTask
import org.bukkit.Location
import org.bukkit.entity.Player
import top.e404.eclean.platform.execution.ExecutionGateway
import top.e404.eclean.platform.execution.PlayerRef
import java.util.UUID

sealed interface TemporaryReturnEvent {
    data object Started : TemporaryReturnEvent
    data object Returned : TemporaryReturnEvent
    data object ReturnedAfterReplace : TemporaryReturnEvent
}

class TemporaryReturnService(
    private val execution: ExecutionGateway,
    private val teleportService: PlayerTeleportService,
    private val notifier: (Player, TemporaryReturnEvent) -> Unit = { _, _ -> },
) {
    private val pendingReturns = mutableMapOf<UUID, PendingReturn>()

    fun teleportWithReturn(player: Player, target: Location, delayTicks: Long) {
        val existing = pendingReturns.remove(player.uniqueId)
        existing?.task?.cancel()
        val origin = (existing?.origin ?: player.location).clone()
        teleportService.teleport(player, target)
        val replaced = existing != null
        pendingReturns[player.uniqueId] = PendingReturn(
            origin = origin,
            task = execution.runLaterForPlayer(player.toRef(), player, delayTicks) {
                completeReturn(player, replaced)
            }
        )
        if (!replaced) notifier(player, TemporaryReturnEvent.Started)
    }

    fun handleQuit(player: Player) {
        val pending = pendingReturns.remove(player.uniqueId) ?: return
        pending.task?.cancel()
        teleportService.teleport(player, pending.origin)
    }

    fun shutdown() {
        pendingReturns.values.forEach { it.task?.cancel() }
        pendingReturns.clear()
    }

    private fun completeReturn(player: Player, replaced: Boolean) {
        val pending = pendingReturns.remove(player.uniqueId) ?: return
        teleportService.teleport(player, pending.origin)
        notifier(
            player,
            if (replaced) TemporaryReturnEvent.ReturnedAfterReplace else TemporaryReturnEvent.Returned
        )
    }

    private fun Player.toRef(): PlayerRef {
        val location = this@toRef.location
        return PlayerRef(
            uniqueId,
            world.name,
            location.x,
            location.y,
            location.z
        )
    }
}

private data class PendingReturn(
    val origin: Location,
    val task: ScheduledTask?,
)
