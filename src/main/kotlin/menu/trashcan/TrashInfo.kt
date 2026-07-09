package top.e404.eclean.menu.trashcan

import org.bukkit.inventory.ItemStack
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.UiDisplayable
import top.e404.eclean.ui.util.editItemMeta

data class TrashInfo(
    val origin: ItemStack,
    var amount: Int,
) : UiDisplayable {
    private val placeholders = arrayOf<Pair<String, Any?>>("amount" to amount)

    override fun update() {
        placeholders[0] = "amount" to amount
        item = generateItem(placeholders)
    }

    override var needUpdate = false
    override var item = generateItem(placeholders)

    private fun generateItem(placeholders: Array<Pair<String, Any?>>) = origin.clone().editItemMeta {
        lore = (lore ?: mutableListOf()).apply {
            addAll(MLang.get("menu.trashcan.item.lore", *placeholders).removeSuffix("\n").lines())
        }
    }.apply { amount = 1 }
}
