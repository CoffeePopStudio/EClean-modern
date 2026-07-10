package top.e404.eclean.feature.trashcan

import org.bukkit.inventory.ItemStack
import java.util.concurrent.locks.ReentrantReadWriteLock

class TrashcanItemStore(private val maxSlots: Int) {
    private val lock = ReentrantReadWriteLock()
    private val items = mutableListOf<ItemStack>()

    val size: Int
        get() {
            lock.readLock().lock()
            try {
                return items.size
            } finally {
                lock.readLock().unlock()
            }
        }

    fun isEmpty(): Boolean {
        lock.readLock().lock()
        try {
            return items.isEmpty()
        } finally {
            lock.readLock().unlock()
        }
    }

    fun loadInto(slots: Array<ItemStack?>) {
        lock.readLock().lock()
        try {
            items.indices.forEach { i -> slots[i] = items[i].clone() }
        } finally {
            lock.readLock().unlock()
        }
    }

    fun saveFrom(slots: Array<ItemStack?>) {
        lock.writeLock().lock()
        try {
            items.clear()
            for (slot in slots) {
                if (slot != null && !slot.type.isAir) {
                    mergeUnsafe(slot.clone())
                }
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    private fun mergeUnsafe(item: ItemStack) {
        val remaining = item
        for (existing in items) {
            if (!existing.isSimilar(remaining)) continue
            val space = existing.type.maxStackSize - existing.amount
            if (space <= 0) continue
            val take = minOf(space, remaining.amount)
            existing.amount += take
            remaining.amount -= take
            if (remaining.amount <= 0) return
        }
        while (remaining.amount > 0) {
            val stackSize = minOf(remaining.type.maxStackSize, remaining.amount)
            items.add(remaining.clone().apply { amount = stackSize })
            remaining.amount -= stackSize
        }
    }

    fun addItem(item: ItemStack): Boolean {
        lock.writeLock().lock()
        try {
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
                if (items.size >= maxSlots) return false
                val stackSize = minOf(remaining.type.maxStackSize, remaining.amount)
                items.add(remaining.clone().apply { amount = stackSize })
                remaining.amount -= stackSize
            }
            return true
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun addAll(stacks: Collection<ItemStack>): Int {
        var added = 0
        for (stack in stacks) {
            if (addItem(stack)) added++
        }
        return added
    }

    fun removeItem(stack: ItemStack, amount: Int): ItemStack? {
        lock.writeLock().lock()
        try {
            val iterator = items.listIterator()
            while (iterator.hasNext()) {
                val existing = iterator.next()
                if (!existing.isSimilar(stack)) continue
                val toRemove = minOf(amount, existing.amount)
                val remainingAmount = existing.amount - toRemove
                if (remainingAmount <= 0) {
                    iterator.remove()
                    return null
                }
                existing.amount = remainingAmount
                return existing.clone()
            }
            return null
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun clear() {
        lock.writeLock().lock()
        try {
            items.clear()
        } finally {
            lock.writeLock().unlock()
        }
    }
}
