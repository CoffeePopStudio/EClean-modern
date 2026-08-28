package top.e404.eclean.menu

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import top.e404.eclean.app.RuntimeServices

object MenuManager : Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        RuntimeServices.temporaryReturnService.handleQuit(event.player)
    }
}
