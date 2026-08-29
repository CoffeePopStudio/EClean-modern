package top.e404.eclean.feature.trashcan

import org.bukkit.inventory.ItemStack
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * 垃圾桶聚合条目
 *
 * @param prototype 该条目的 isSimilar 键(amount 恒为 1), 用于合并/匹配
 * @param count     聚合后的总数量, 可超过 maxStackSize
 * @param deadline  条目到期时间戳(epoch millis), Long.MAX_VALUE 表示永不过期
 */
data class TrashcanEntry(
    val prototype: ItemStack,
    var count: Long,
    var deadline: Long,
)

/**
 * 垃圾桶物品存储: 按 isSimilar 聚合成条目, 每个条目数量不限
 *
 * @param lifetimeSeconds 条目存活秒数(读取时求值), null 表示永不过期
 * @param stackingEnabled 是否启用聚合, false 时每次放入都新建条目
 */
class TrashcanItemStore(
    private val lifetimeSeconds: () -> Long?,
    private val stackingEnabled: () -> Boolean = { true },
) {
    private val lock = ReentrantReadWriteLock()
    private val entries = mutableListOf<TrashcanEntry>()

    val size: Int
        get() {
            lock.readLock().lock()
            try {
                return entries.size
            } finally {
                lock.readLock().unlock()
            }
        }

    fun isEmpty(): Boolean {
        lock.readLock().lock()
        try {
            return entries.isEmpty()
        } finally {
            lock.readLock().unlock()
        }
    }

    fun totalCount(): Long {
        lock.readLock().lock()
        try {
            return entries.sumOf { it.count }
        } finally {
            lock.readLock().unlock()
        }
    }

    /** 放入物品: 合并到相似条目(不重置其到期时间), 否则新建条目; 无限容量, 恒为 true */
    fun addItem(item: ItemStack): Boolean {
        val stacking = stackingEnabled()
        val lifetime = lifetimeSeconds()
        lock.writeLock().lock()
        try {
            val amount = item.amount.toLong()
            if (stacking) {
                for (entry in entries) {
                    if (!entry.prototype.isSimilar(item)) continue
                    entry.count += amount
                    return true
                }
            }
            entries.add(
                TrashcanEntry(
                    prototype = item.clone().apply { this.amount = 1 },
                    count = amount,
                    deadline = if (lifetime == null) Long.MAX_VALUE else System.currentTimeMillis() + lifetime * 1000,
                )
            )
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

    /** 取出物品: 从相似条目中扣除数量, 扣完的条目移除; 返回实际取出的数量 */
    fun removeItem(prototype: ItemStack, amount: Int): Int {
        lock.writeLock().lock()
        try {
            var remaining = amount.toLong()
            val iterator = entries.listIterator()
            while (iterator.hasNext() && remaining > 0) {
                val entry = iterator.next()
                if (!entry.prototype.isSimilar(prototype)) continue
                val toRemove = minOf(remaining, entry.count)
                val newCount = entry.count - toRemove
                if (newCount <= 0) {
                    iterator.remove()
                } else {
                    entry.count = newCount
                }
                remaining -= toRemove
            }
            return (amount - remaining).toInt()
        } finally {
            lock.writeLock().unlock()
        }
    }

    /** 移除所有已到期的条目, 返回移除的条目数 */
    fun expireEntries(nowMillis: Long): Int {
        lock.writeLock().lock()
        try {
            val before = entries.size
            entries.removeAll { it.deadline <= nowMillis }
            return before - entries.size
        } finally {
            lock.writeLock().unlock()
        }
    }

    /** 条目快照(插入顺序), 每个条目为独立拷贝 */
    fun getEntries(): List<TrashcanEntry> {
        lock.readLock().lock()
        try {
            return entries.map { it.copy(prototype = it.prototype.clone()) }
        } finally {
            lock.readLock().unlock()
        }
    }

    /** 最早到期条目的时间戳; 没有条目或全部永不过期时返回 null */
    fun earliestDeadline(): Long? {
        lock.readLock().lock()
        try {
            return entries.asSequence()
                .map { it.deadline }
                .filter { it != Long.MAX_VALUE }
                .minOrNull()
        } finally {
            lock.readLock().unlock()
        }
    }

    fun clear() {
        lock.writeLock().lock()
        try {
            entries.clear()
        } finally {
            lock.writeLock().unlock()
        }
    }
}
