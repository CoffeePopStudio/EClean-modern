package top.e404.eclean.feature.trashcan

import org.bukkit.inventory.ItemStack

data class TrashcanEntry(
    val origin: ItemStack,
    var amount: Int,
)
