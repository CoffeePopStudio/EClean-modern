package top.e404.eclean.menu

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import top.e404.eclean.PL
import top.e404.eclean.menu.trashcan.TrashcanMenu
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.ui.UiMenu
import java.util.concurrent.ConcurrentHashMap

object MenuManager : Listener {
    private val openMenus = ConcurrentHashMap<Player, UiMenu>()

    fun openMenu(menu: UiMenu, player: Player) {
        val previous = openMenus.put(player, menu)
        if (previous != null && previous !== menu) {
            previous.unregister()
            player.closeInventory()
        }
        menu.open(player)
    }

    fun getOpenMenu(player: Player): UiMenu? = openMenus[player]

    fun hasOpenMenus(): Boolean = openMenus.isNotEmpty()

    fun forEachOpenMenu(action: (UiMenu) -> Unit) {
        openMenus.values.toList().forEach(action)
    }

    fun refreshTrashcanMenus() {
        openMenus.entries.toList().forEach { (player, menu) ->
            if (menu is TrashcanMenu) {
                Schedulers.runForEntity(player) {
                    menu.rebuildDisplayData()
                    menu.updateIcon()
                }
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
        PL.services.temporaryReturnService.handleQuit(event.player)
    }

    @EventHandler
    fun onAsyncChat(event: AsyncChatEvent) {
        val player = event.player
        val menu = openMenus[player]
        if (menu is TrashcanMenu && menu.isSearching) {
            event.isCancelled = true
            val query = PlainTextComponentSerializer.plainText().serialize(event.message())
            Schedulers.runForEntity(player) {
                if (query.equals("cancel", true)) menu.applySearchQuery(null) else menu.applySearchQuery(query)
            }
        }
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
