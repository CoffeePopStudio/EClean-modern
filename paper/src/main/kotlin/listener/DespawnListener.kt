package top.e404.eclean.listener
import top.e404.eclean.PL

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemDespawnEvent
import top.e404.eclean.config.Config
import top.e404.eclean.config.matches

object DespawnListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun ItemDespawnEvent.onEvent() {
        val item = entity.itemStack
        PL.services.messages.debug { "Item despawned: ${item.type.name}, world: ${entity.world.name}" }
        Config.current.trashcan.run {
            if (!enabled
                || !despawnRecovery.enabled
                || despawnRecovery.disabledWorlds.matches(entity.world.name)
                || !despawnRecovery.matchers.matches(item.type.name)
            ) return
        }
        PL.services.messages.debug { "Trashcan recovery: ${item.type.name}, world: ${entity.world.name}" }
        val accepted = PL.services.trashcanManager.addItem(item.clone())
        if (!accepted) {
            PL.services.messages.debug { "Trashcan full, despawn recovery skipped for: ${item.type.name}" }
            return
        }
        entity.remove()
    }
}
