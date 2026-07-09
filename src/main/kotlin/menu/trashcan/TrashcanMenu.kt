package top.e404.eclean.menu.trashcan

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.lang.MLang
import top.e404.eclean.feature.trashcan.TrashcanMenuModel
import top.e404.eclean.ui.UiMenu

class TrashcanMenu : UiMenu(PL, MLang["menu.trashcan.title"], 6, true) {
    private val model = TrashcanMenuModel(RuntimeServices.trashcanRepository)
    val zone = TrashcanZone(this, model.entries())
    private val prev = PrevButton(this)
    private val next = NextButton(this)

    init {
        onPlayerInvClick = { event -> zone.onClickSelfInv(event) }
        initSlots(
            listOf(
                "         ",
                "         ",
                "         ",
                "         ",
                "         ",
                "  p   n  ",
            )
        ) { char ->
            when (char) {
                'p' -> prev.button
                'n' -> next.button
                else -> null
            }
        }
        addPager(zone.pager)
    }

    override fun open(player: Player) {
        super.open(player)
        opened.add(this)
        ensureCloseListener()
    }

    override fun onInventoryClick(event: InventoryClickEvent) {
        super.onInventoryClick(event)
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
                    opened.removeAll { it.inventory == event.inventory }
                }
            }, PL)
        }

        fun updateAllOpen() {
            opened.toSet().forEach { it.updateIcon() }
        }
    }
}
