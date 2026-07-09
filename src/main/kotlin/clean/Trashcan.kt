package top.e404.eclean.clean

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config

object Trashcan : Listener {
    val trashData get() = RuntimeServices.trashcanRepository.trashData

    val trashValues get() = RuntimeServices.trashcanRepository.trashValues

    fun cleanTrash(){
        RuntimeServices.trashcanService.clearAll()
    }

    val countdown get() = RuntimeServices.trashcanService.countdown

    fun schedule() {
        RuntimeServices.trashcanTicker.start()
    }

    fun ItemStack.sign() = ItemSign(this)
    class ItemSign(val item: ItemStack) {
        override fun equals(other: Any?): Boolean {
            if (other == null) return false
            if (other !is ItemSign) return false
            return item.isSimilar(other.item)
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + item.type.hashCode()
            @Suppress("DEPRECATION")
            hash = 31 * hash + item.durability.toInt()
            val enchants = item.itemMeta?.enchants
            if (enchants != null) {
                for ((enchant, level) in enchants.entries.sortedBy { it.key.key.key }) {
                    hash = 31 * hash + enchant.key.key.hashCode()
                    hash = 31 * hash + level
                }
            }
            return hash
        }
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
