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
            val count = minOf(items.size, slots.size)
            for (i in 0 until count) {
                slots[i] = items[i].clone()
            }
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

    fun removeItem(stack: ItemStack, amount: Int): Int {
        lock.writeLock().lock()
        try {
            var remaining = amount
            val iterator = items.listIterator()
            while (iterator.hasNext() && remaining > 0) {
                val existing = iterator.next()
                if (!existing.isSimilar(stack)) continue
                val toRemove = minOf(remaining, existing.amount)
                val remainingAmount = existing.amount - toRemove
                if (remainingAmount <= 0) {
                    iterator.remove()
                } else {
                    existing.amount = remainingAmount
                }
                remaining -= toRemove
            }
            return amount - remaining
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun getSnapshot(): List<ItemStack> {
        lock.readLock().lock()
        try {
            return items.map { it.clone() }
        } finally {
            lock.readLock().unlock()
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
