package top.e404.eclean.service

import org.bukkit.Location
import org.bukkit.entity.Player
import top.e404.eclean.platform.execution.ExecutionGateway
import top.e404.eclean.platform.execution.PlayerRef

class PlayerTeleportService(
    private val execution: ExecutionGateway,
) {
    fun teleport(player: Player, target: Location) {
        execution.runForPlayer(player.toRef(), player) {
            player.teleport(target)
        }
    }

    private fun Player.toRef(): PlayerRef {
        val location = location
        return PlayerRef(uniqueId, world.name, location.x, location.y, location.z)
    }
}
