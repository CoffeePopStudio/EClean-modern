package top.e404.eclean.ui.util

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun buildItemStack(
    material: Material,
    amount: Int = 1,
    name: String? = null,
    lore: List<String>? = null,
    block: (ItemMeta.() -> Unit)? = null,
): ItemStack {
    val item = ItemStack(material, amount)
    val meta = item.itemMeta ?: return item
    if (name != null) meta.displayName(MiniMessage.miniMessage().deserialize(name))
    if (lore != null) meta.lore(lore.map { MiniMessage.miniMessage().deserialize(it) })
    block?.invoke(meta)
    item.itemMeta = meta
    return item
}

val emptyItem = ItemStack(Material.AIR)

fun ItemStack.editItemMeta(block: ItemMeta.() -> Unit): ItemStack {
    val meta = itemMeta ?: return this
    block(meta)
    itemMeta = meta
    return this
}

fun <T> List<T>.splitByPage(pageSize: Int, page: Int): List<T> {
    val start = page * pageSize
    if (start >= size) return emptyList()
    return subList(start, minOf(start + pageSize, size))
}
