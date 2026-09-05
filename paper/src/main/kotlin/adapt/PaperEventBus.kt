package top.e404.eclean.paper.adapt

import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import top.e404.eclean.common.api.EventBus

class PaperEventBus(
    private val plugin: Plugin,
) : EventBus {

    override fun register(listener: Any) {
        if (listener is Listener) {
            Bukkit.getPluginManager().registerEvents(listener, plugin)
        }
    }

    override fun unregister(listener: Any) {
        if (listener is Listener) {
            HandlerList.unregisterAll(listener)
        }
    }
}
