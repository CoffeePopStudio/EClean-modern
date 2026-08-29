package top.e404.eclean.menu

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.menu.trashcan.TrashcanMenu
import top.e404.eclean.ui.UiMenu

object MenuManager : Listener {
    private val openMenus = mutableMapOf<Player, UiMenu>()

    fun openMenu(menu: UiMenu, player: Player) {
        openMenus[player] = menu
        menu.open(player)
    }

    fun getOpenMenu(player: Player): UiMenu? = openMenus[player]

    fun hasOpenMenus(): Boolean = openMenus.isNotEmpty()

    fun forEachOpenMenu(action: (UiMenu) -> Unit) {
        openMenus.values.toList().forEach(action)
    }

    fun refreshTrashcanMenus() {
        forEachOpenMenu { menu ->
            if (menu is TrashcanMenu) {
                menu.rebuildDisplayData()
                menu.updateIcon()
            }
        }
    }

    fun closeMenus() {
        for ((player, menu) in HashMap(openMenus)) {
            menu.unregister()
            player.closeInventory()
        }
        openMenus.clear()
    }

    fun shutdown() {
        closeMenus()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        openMenus.remove(event.player)?.unregister()
        RuntimeServices.temporaryReturnService.handleQuit(event.player)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val menu = openMenus[player]
        if (menu != null && menu.inventory == event.inventory) {
            openMenus.remove(player)
            menu.unregister()
        }
    }
}
