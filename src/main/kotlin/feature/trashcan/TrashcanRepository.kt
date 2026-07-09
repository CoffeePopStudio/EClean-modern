package top.e404.eclean.feature.trashcan

import org.bukkit.inventory.ItemStack
import top.e404.eclean.clean.Trashcan
import top.e404.eclean.menu.trashcan.TrashInfo

class TrashcanRepository {
    val trashData: MutableMap<Trashcan.ItemSign, TrashInfo> = mutableMapOf()
    val trashValues: MutableList<TrashInfo> = mutableListOf()

    @Synchronized
    fun upsert(item: ItemStack) {
        val cloned = item.clone()
        val sign = Trashcan.ItemSign(cloned)
        val exists = trashData[sign]
        if (exists != null) {
            exists.amount += cloned.amount
        } else {
            trashData[sign] = TrashInfo(cloned, cloned.amount)
        }
        syncValues()
    }

    @Synchronized
    fun removeBySign(sign: Trashcan.ItemSign) {
        trashData.remove(sign)
        syncValues()
    }

    @Synchronized
    fun removeEmpty() {
        trashData.entries.removeIf { it.value.amount <= 0 }
        syncValues()
    }

    @Synchronized
    fun clear() {
        trashData.clear()
        trashValues.clear()
    }

    private fun syncValues() {
        trashValues.clear()
        trashValues.addAll(trashData.values)
    }
}
