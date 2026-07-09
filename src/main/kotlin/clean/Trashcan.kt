package top.e404.eclean.clean

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config

object Trashcan : Listener {
    val trashValues get() = RuntimeServices.trashcanRepository.trashValues

    fun cleanTrash(){
        RuntimeServices.trashcanService.clearAll()
    }

    val countdown get() = RuntimeServices.trashcanService.countdown

    fun schedule() {
        RuntimeServices.trashcanTicker.start()
    }

    @EventHandler
    fun InventoryCloseEvent.onEvent() {
    }

    fun open(player: org.bukkit.entity.Player) {
        RuntimeServices.trashcanService.open(player)
    }

    fun addItems(items: Collection<ItemStack>) {
        RuntimeServices.trashcanService.collectStacks(items)
    }

    fun addItem(item: ItemStack) {
        RuntimeServices.trashcanService.addItem(item)
    }

    fun update() {
        RuntimeServices.trashcanService.updateMenus()
    }
}
