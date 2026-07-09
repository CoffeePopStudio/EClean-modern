package top.e404.eclean.menu.dense

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.clean.info
import top.e404.eclean.lang.MLang
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.platform.execution.ChunkRef
import top.e404.eplugin.menu.zone.MenuButtonZone

class DenseZone(
    override val menu: DenseMenu,
    override val data: MutableList<EntityInfo>
) : MenuButtonZone<EntityInfo>(menu, 0, 0, 9, 5, data) {
    override val inv = menu.inv
    override fun onClick(menuIndex: Int, zoneIndex: Int, itemIndex: Int, event: InventoryClickEvent): Boolean {
        val info = data.getOrNull(itemIndex) ?: return true
        val player = event.whoClicked as Player
        if (event.isRightClick) {
            handleRightClick(player, info.chunk, info.type, itemIndex)
        } else {
            handleTeleport(player, info.chunk, menu.temp)
        }
        return true
    }

    private fun handleRightClick(player: Player, chunkRef: ChunkRef, type: org.bukkit.entity.EntityType, itemIndex: Int) {
        val world = player.server.getWorld(chunkRef.world) ?: return
        val loc = Location(world, chunkRef.x * 16.0 + 8.0, 64.0, chunkRef.z * 16.0 + 8.0)
        Schedulers.runAtLocation(loc) {
            val chunk = world.getChunkAt(chunkRef.x, chunkRef.z)
            val entities = chunk.entities.filter { it.type == type }
            RuntimeServices.messages.send(
                player,
                MLang[
                    "menu.dense.clean",
                    "chunk" to chunkRef.info(),
                    "type" to type.name,
                    "count" to entities.size
                ]
            )
            entities.forEach(Entity::remove)
        }
        data.removeAt(itemIndex)
        Schedulers.runGlobal {
            menu.updateIcon()
        }
    }

    private fun handleTeleport(player: Player, chunkRef: ChunkRef, temp: Boolean) {
        val world = player.server.getWorld(chunkRef.world) ?: return
        val x = chunkRef.x * 16 + 8
        val z = chunkRef.z * 16 + 8
        val loc = Location(world, x + 0.5, 0.0, z + 0.5)
        Schedulers.runAtLocation(loc) {
            val y = world.getHighestBlockYAt(x, z)
            val target = Location(world, x + 0.5, y + 1.0, z + 0.5)
            if (!temp) {
                RuntimeServices.playerTeleportService.teleport(player, target)
                RuntimeServices.messages.send(player, MLang["command.teleport.done"])
            } else {
                RuntimeServices.temporaryReturnService.teleportWithReturn(player, target, 600)
            }
        }
    }
}
