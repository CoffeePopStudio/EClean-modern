package top.e404.eclean.menu.trashcan

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import top.e404.eclean.PL
import top.e404.eclean.lang.MLang
import top.e404.eclean.feature.trashcan.TrashcanRepository
import top.e404.eclean.ui.UiMenu

class TrashcanMenu(
    private val repository: TrashcanRepository,
) : UiMenu(PL, MLang["menu.trashcan.title"], 6, false) {

    init {
        val contents = inventory.contents
        repository.loadInto(contents)
        inventory.contents = contents
    }

    override fun open(player: Player) {
        super.open(player)
        opened.add(this)
        ensureCloseListener()
    }

    private fun saveAndClose(event: InventoryCloseEvent) {
        repository.saveFrom(inventory.contents)
        unregister()
        opened.remove(this)
    }

    companion object {
        val opened = mutableSetOf<TrashcanMenu>()
        private var closeListenerRegistered = false

        private fun ensureCloseListener() {
            if (closeListenerRegistered) return
            closeListenerRegistered = true
            PL.server.pluginManager.registerEvents(object : Listener {
                @EventHandler
                fun onClose(event: InventoryCloseEvent) {
                    opened.find { it.inventory == event.inventory }?.saveAndClose(event)
                }
            }, PL)
        }

        fun updateAllOpen() {
            opened.toSet().forEach { menu ->
                val contents = menu.inventory.contents
                menu.repository.loadInto(contents)
                menu.inventory.contents = contents
            }
        }
    }
}
