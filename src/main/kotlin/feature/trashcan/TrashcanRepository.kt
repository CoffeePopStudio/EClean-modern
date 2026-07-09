package top.e404.eclean.feature.trashcan

import org.bukkit.inventory.ItemStack
import java.util.concurrent.CopyOnWriteArrayList

class TrashcanRepository {
    private val items = CopyOnWriteArrayList<ItemStack>()

    fun loadInto(slots: Array<ItemStack?>) {
        items.forEachIndexed { i, item -> if (i < slots.size) slots[i] = item.clone() }
    }

    fun saveFrom(slots: Array<ItemStack?>) {
        items.clear()
        for (slot in slots) {
            if (slot != null && !slot.type.isAir) {
                items.add(slot.clone())
            }
        }
    }

    fun addItem(item: ItemStack) {
        val remaining = item.clone()
        for (existing in items) {
            if (!existing.isSimilar(remaining)) continue
            val space = existing.type.maxStackSize - existing.amount
            if (space <= 0) continue
            val take = minOf(space, remaining.amount)
            existing.amount += take
            remaining.amount -= take
            if (remaining.amount <= 0) break
        }
        while (remaining.amount > 0) {
            val stackSize = minOf(remaining.type.maxStackSize, remaining.amount)
            items.add(remaining.clone().apply { amount = stackSize })
            remaining.amount -= stackSize
        }
    }

    fun addAll(stacks: Collection<ItemStack>) {
        stacks.forEach { addItem(it) }
    }

    fun isEmpty() = items.isEmpty()

    fun clear() {
        items.clear()
    }
}
