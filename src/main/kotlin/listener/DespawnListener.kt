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
        RuntimeServices.messages.debug { "物品到时间后销毁: ${item.type.name}, 世界: ${entity.world.name}" }
        Config.current.trashcan.run {
            if (!enabled
                || !despawnRecovery.enabled
                || despawnRecovery.disabledWorlds.matches(entity.world.name)
                || !despawnRecovery.matchers.matches(item.type.name)
            ) return
        }
        RuntimeServices.messages.debug { "回收匹配物品到垃圾桶: ${item.type.name}, 世界: ${entity.world.name}" }
        Trashcan.addItem(item.clone())
        entity.remove()
    }
}
