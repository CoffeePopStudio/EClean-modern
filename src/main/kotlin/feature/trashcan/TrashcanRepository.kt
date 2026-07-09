package top.e404.eclean.feature.trashcan

import org.bukkit.inventory.ItemStack
import top.e404.eclean.menu.trashcan.TrashInfo
import java.util.concurrent.CopyOnWriteArrayList

class TrashcanRepository {
    private val entries = CopyOnWriteArrayList<TrashInfo>()
    val trashValues: MutableList<TrashInfo> = entries

    @Synchronized
    fun upsert(item: ItemStack) {
        val cloned = item.clone()
        val existing = entries.find { it.origin.isSimilar(cloned) }
        if (existing != null) {
            existing.amount += cloned.amount
        } else {
            entries.add(TrashInfo(cloned, cloned.amount))
        }
    }

    @Synchronized
    fun removeByItem(item: ItemStack) {
        entries.removeAll { it.origin.isSimilar(item) }
    }

    @Synchronized
    fun clear() {
        entries.clear()
    }
}
