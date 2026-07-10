package top.e404.eclean.menu.trashcan

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.inventory.ItemStack
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.UiDisplayable

class TrashcanDisplayItem(
    val snapshot: ItemStack,
) : UiDisplayable {
    private val miniMessage = MiniMessage.miniMessage()

    override var needUpdate = true
    override lateinit var item: ItemStack

    override fun update() {
        item = buildDisplayItem()
        needUpdate = false
    }

    private fun buildDisplayItem(): ItemStack {
        val clone = snapshot.clone()
        val realAmount = clone.amount
        clone.amount = 1
        val meta = clone.itemMeta ?: return clone
        val rawLore = MLang["menu.trashcan.item.lore", "amount" to realAmount.toString()]
        val lines = rawLore.split("\n").map { miniMessage.deserialize(it) }
        meta.lore(lines)
        clone.itemMeta = meta
        return clone
    }

    val stackType get() = snapshot.type
    val stackAmount get() = snapshot.amount
}
