package top.e404.eclean.menu

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eplugin.menu.EMenuManager

object MenuManager : EMenuManager(PL) {
    @EventHandler
    fun PlayerQuitEvent.onEvent() {
        RuntimeServices.temporaryReturnService.handleQuit(player)
    }
}
