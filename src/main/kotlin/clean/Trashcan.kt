package top.e404.eclean.clean

import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config
import top.e404.eclean.menu.trashcan.TrashInfo
import top.e404.eplugin.listener.EListener

object Trashcan : EListener(PL) {
    /**
     * 垃圾桶中的物品及其数量, ItemStack的数量没有作用, 以value的数量为准
     */
    val trashData get() = RuntimeServices.trashcanRepository.trashData

    val trashValues get() = RuntimeServices.trashcanRepository.trashValues

    fun cleanTrash(){
        RuntimeServices.trashcanService.clearAll()
    }

    /**
     * 垃圾桶清空倒计时, 单位秒
     */
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
            hash = hash * 31 + item.type.hashCode()
            @Suppress("DEPRECATION")
            hash = hash * 31 + (item.durability.toInt() and 0xffff)
            if (item.hasItemMeta()) hash = hash * 31 + item.itemMeta.hashCode()
            return hash
        }
    }

    @EventHandler
    fun InventoryCloseEvent.onEvent() {
        // 菜单更新改为基于 MenuManager 的当前菜单快照，这里不再维护独立的打开集合
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
