package top.e404.eclean.ui

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface UiDisplayable {
    fun update()
    val item: ItemStack
    var needUpdate: Boolean
}

class UiPager<T : UiDisplayable>(
    private val data: MutableList<T>,
    private val pageSize: Int = 45,
    private val startSlot: Int = 0,
    private val onClickHandler: (Int, InventoryClickEvent) -> Boolean,
) {
    var page = 0
        private set

    val hasPrev get() = page > 0
    val hasNext get() = (page + 1) * pageSize < data.size

    fun nextPage() { if (hasNext) page++ }
    fun prevPage() { if (hasPrev) page-- }

    fun render(inventory: Inventory) {
        val start = page * pageSize
        val end = minOf(start + pageSize, data.size)
        for (i in start until end) {
            val displayable = data[i]
            if (displayable.needUpdate) displayable.update()
            inventory.setItem(startSlot + (i - start), displayable.item)
        }
        for (i in (end - start) until pageSize) {
            inventory.setItem(startSlot + i, emptyItem)
        }
    }

    fun onClick(slot: Int, event: InventoryClickEvent): Boolean {
        val index = page * pageSize + (slot - startSlot)
        if (index < 0 || index >= data.size) return false
        return onClickHandler(index, event)
    }

    fun get(index: Int): T? = data.getOrNull(index)
    fun removeAt(index: Int) = data.removeAt(index)
}
