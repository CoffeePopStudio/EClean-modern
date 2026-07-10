package top.e404.eclean.menu.trashcan

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import top.e404.eclean.feature.trashcan.TrashcanItemStore
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.UiButton

object TrashcanItemButton {
    private val miniMessage = MiniMessage.miniMessage()

    fun create(
        store: TrashcanItemStore,
        itemStack: ItemStack,
        onChanged: () -> Unit,
    ): UiButton {
        return UiButton(
            initialItem = buildItem(itemStack),
            onClickHandler = { event -> handleClick(store, itemStack, event, onChanged) },
        )
    }

    fun rebuild(itemStack: ItemStack): ItemStack = buildItem(itemStack)

    private fun buildItem(itemStack: ItemStack): ItemStack {
        val item = itemStack.clone()
        val realAmount = item.amount
        item.amount = 1
        val meta = item.itemMeta ?: return item
        val rawLore = MLang["menu.trashcan.item.lore", "amount" to realAmount.toString()]
        val lines = rawLore.split("\n").map { miniMessage.deserialize(it) }
        meta.lore(lines)
        item.itemMeta = meta
        return item
    }

    private fun handleClick(
        store: TrashcanItemStore,
        itemStack: ItemStack,
        event: InventoryClickEvent,
        onChanged: () -> Unit,
    ): Boolean {
        val player = event.whoClicked as? Player ?: return false
        val take = when (event.click) {
            ClickType.LEFT -> if (event.isShiftClick) itemStack.type.maxStackSize else 1
            ClickType.RIGHT -> maxOf(1, itemStack.amount / 2)
            else -> return false
        }
        var waitForTake = minOf(take, itemStack.amount)
        if (waitForTake <= 0) return true
        val maxStackSize = itemStack.type.maxStackSize
        for (i in 0 until 36) {
            if (waitForTake == 0) break
            val slotItem = player.inventory.getItem(i)
            if (slotItem == null || slotItem.type == Material.AIR) {
                val count = minOf(waitForTake, maxStackSize)
                waitForTake -= count
                player.inventory.setItem(i, itemStack.clone().apply { amount = count })
                continue
            }
            if (!slotItem.isSimilar(itemStack)) continue
            if (slotItem.amount >= maxStackSize) continue
            val count = minOf(waitForTake, maxStackSize - slotItem.amount)
            waitForTake -= count
            player.inventory.setItem(i, slotItem.clone().apply { amount += count })
        }
        val given = minOf(take, itemStack.amount) - waitForTake
        if (given <= 0) return true
        store.removeItem(itemStack, given)
        onChanged()
        return true
    }
}
