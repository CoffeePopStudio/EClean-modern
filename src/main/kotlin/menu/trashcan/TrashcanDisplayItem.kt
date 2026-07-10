package top.e404.eclean.menu.trashcan

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.inventory.ItemStack
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.UiDisplayable
import top.e404.eclean.ui.editItemMeta

class TrashcanDisplayItem(
    val snapshot: ItemStack,
) : UiDisplayable {
    override var needUpdate = true
    override lateinit var item: ItemStack

    override fun update() {
        item = generateItem()
        needUpdate = false
    }

    private fun generateItem() = snapshot.clone().editItemMeta {
        val placeholders = arrayOf<Pair<String, *>>("amount" to snapshot.clone().amount)
        val existingLore = lore() ?: mutableListOf()
        val newLines = MLang.get("menu.trashcan.item.lore", *placeholders)
            .removeSuffix("\n")
            .lines()
            .map { MiniMessage.miniMessage().deserialize(it) }
        existingLore.addAll(newLines)
        lore(existingLore)
    }.apply { amount = 1 }

    val stackType get() = snapshot.type
    val stackAmount get() = snapshot.amount
}
