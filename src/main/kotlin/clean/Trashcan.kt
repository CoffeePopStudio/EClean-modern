package top.e404.eclean.clean

import org.bukkit.inventory.ItemStack
import top.e404.eclean.app.RuntimeServices

object Trashcan {
    fun cleanTrash() {
        RuntimeServices.trashcanService.clearAll()
    }

    val countdown get() = RuntimeServices.trashcanService.countdown

    fun schedule() {
        RuntimeServices.trashcanTicker.start()
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
}
