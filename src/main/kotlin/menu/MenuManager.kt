package top.e404.eclean.menu

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.ui.UiMenu

object MenuManager : Listener {
    private val openMenus = mutableMapOf<Player, UiMenu>()

    fun openMenu(menu: UiMenu, player: Player) {
        openMenus[player] = menu
        menu.open(player)
    }

    fun closeMenus() {
        for ((player, menu) in HashMap(openMenus)) {
            menu.unregister()
            player.closeInventory()
        }
        openMenus.clear()
    }

    fun getOpenMenu(player: Player): UiMenu? = openMenus[player]

    fun shutdown() {
        closeMenus()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        openMenus.remove(event.player)?.unregister()
        RuntimeServices.temporaryReturnService.handleQuit(event.player)
    }
}
