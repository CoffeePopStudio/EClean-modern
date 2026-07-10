package top.e404.eclean.menu.trashcan

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import top.e404.eclean.PL
import top.e404.eclean.feature.trashcan.TrashcanItemStore
import top.e404.eclean.feature.trashcan.TrashcanManager
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.UiMenu

class TrashcanMenu(
    private val store: TrashcanItemStore,
    private val manager: TrashcanManager,
) : UiMenu(PL, MLang["trash.title"], 6, false) {

    init {
        rebuildButtons()
    }

    private fun rebuildButtons() {
        for (slot in 0 until 54) setButton(slot, null)
        val contents = inventory.contents
        store.loadInto(contents)
        for ((slot, item) in contents.withIndex()) {
            if (item == null || item.type.isAir) continue
            setButton(
                slot,
                TrashcanItemButton.create(store, item, ::rebuildButtons)
            )
        }
        updateIcon()
    }

    override fun open(player: Player) {
        super.open(player)
        opened.add(this)
        ensureCloseListener()
    }

    override fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory != inventory) return

        val clickedInventory = event.clickedInventory ?: return

        if (clickedInventory != inventory) {
            handlePlayerInvClick(event)
            return
        }

        val slot = event.slot
        val button = buttons[slot]
        if (button != null) {
            event.isCancelled = true
            button.onClick(event)
            return
        }

        event.isCancelled = false
    }

    private fun handlePlayerInvClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val clicked = event.currentItem ?: return
        if (clicked.type == Material.AIR) return

        event.isCancelled = true

        val count = when (event.click) {
            ClickType.LEFT, ClickType.DOUBLE_CLICK -> 1
            ClickType.SHIFT_LEFT -> clicked.amount
            ClickType.RIGHT -> maxOf(clicked.amount / 2, 1)
            else -> return
        }

        if (count == clicked.amount) {
            player.inventory.setItem(event.slot, null)
        } else {
            clicked.amount -= count
        }

        val putItem = clicked.clone()
        putItem.amount = count
        store.addItem(putItem)
        rebuildButtons()
    }

    private fun saveAndClose(event: InventoryCloseEvent) {
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
            opened.toSet().forEach { it.rebuildButtons() }
        }
    }
}
