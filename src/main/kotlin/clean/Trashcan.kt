package top.e404.eclean.clean

import org.bukkit.inventory.ItemStack
import top.e404.eclean.app.RuntimeServices

object Trashcan {
    fun cleanTrash() {
        RuntimeServices.trashcanManager.clearAll()
    }

    fun open(player: org.bukkit.entity.Player) {
        RuntimeServices.trashcanManager.open(player)
    }

    fun addItems(items: Collection<ItemStack>) {
        RuntimeServices.trashcanManager.collectStacks(items)
    }

    fun addItem(item: ItemStack): Boolean {
        return RuntimeServices.trashcanManager.addItem(item)
    }
}
