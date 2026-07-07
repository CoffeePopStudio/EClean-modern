package top.e404.eclean.feature.trashcan

import org.bukkit.inventory.ItemStack
import top.e404.eclean.clean.Trashcan
import top.e404.eclean.menu.trashcan.TrashInfo

class TrashcanRepository {
    val trashData: MutableMap<Trashcan.ItemSign, TrashInfo> = mutableMapOf()
    val trashValues: MutableList<TrashInfo> = mutableListOf()

    fun upsert(item: ItemStack) {
        val sign = Trashcan.ItemSign(item)
        val exists = trashData[sign]
        if (exists != null) {
            exists.amount += item.amount
        } else {
            trashData[sign] = TrashInfo(item, item.amount)
        }
        syncValues()
    }

    fun removeEmpty() {
        trashData.entries.removeIf { it.value.amount <= 0 }
        syncValues()
    }

    fun clear() {
        trashData.clear()
        trashValues.clear()
    }

    fun syncValues() {
        trashValues.clear()
        trashValues.addAll(trashData.values)
    }
}
