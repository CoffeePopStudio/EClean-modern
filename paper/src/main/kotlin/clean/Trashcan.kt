package top.e404.eclean.clean
import top.e404.eclean.PL

import org.bukkit.inventory.ItemStack

object Trashcan {
    fun cleanTrash() {
        PL.services.trashcanManager.clearAll()
    }

    fun open(player: org.bukkit.entity.Player) {
        PL.services.trashcanManager.open(player)
    }

    fun addItems(items: Collection<ItemStack>) {
        PL.services.trashcanManager.collectStacks(items)
    }

    fun addItem(item: ItemStack): Boolean {
        return PL.services.trashcanManager.addItem(item)
    }
}
