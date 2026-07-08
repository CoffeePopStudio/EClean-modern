package top.e404.eclean.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemDespawnEvent
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.clean.Trashcan
import top.e404.eclean.config.Config
import top.e404.eclean.config.matches

object DespawnListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun ItemDespawnEvent.onEvent() {
        val item = entity.itemStack
        RuntimeServices.messages.debug { "Item despawned: ${item.type.name}, world: ${entity.world.name}" }
        Config.current.trashcan.run {
            if (!enabled
                || !despawnRecovery.enabled
                || despawnRecovery.disabledWorlds.matches(entity.world.name)
                || !despawnRecovery.matchers.matches(item.type.name)
            ) return
        }
        RuntimeServices.messages.debug { "Trashcan recovery: ${item.type.name}, world: ${entity.world.name}" }
        Trashcan.addItem(item.clone())
        entity.remove()
    }
}
