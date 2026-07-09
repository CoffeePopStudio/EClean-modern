package top.e404.eclean.menu.trashcan

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.clean.Trashcan
import top.e404.eclean.clean.Trashcan.sign
import top.e404.eclean.ui.UiPager
import top.e404.eclean.ui.util.emptyItem
import kotlin.math.max
import kotlin.math.min

class TrashcanZone(
    val menu: TrashcanMenu,
    private val data: MutableList<TrashInfo>,
) {
    val pager = UiPager(
        data = data,
        pageSize = 45,
        startSlot = 0,
        onClickHandler = handler@{ itemIndex, event ->
            val player = event.whoClicked as Player
            val info = data.getOrNull(itemIndex) ?: return@handler true
            val planTake = when (event.click) {
                ClickType.LEFT, ClickType.DOUBLE_CLICK -> 1
                ClickType.SHIFT_LEFT -> info.item.maxStackSize
                ClickType.RIGHT -> max(min(info.item.maxStackSize / 2, info.amount / 2), 1)
                else -> {
                    player.playSound(player.location, Sound.ENTITY_BLAZE_DEATH, 1F, 1F)
                    return@handler true
                }
            }.let { min(it, info.amount) }

            var waitForTake = planTake
            RuntimeServices.messages.debug { "Player ${player.name} plans to take ${info.origin.type}x${planTake} from trashcan (remain: ${info.amount - planTake})" }
            val maxStackSize = info.origin.type.maxStackSize
            for (i in (0 until 36)) {
                if (waitForTake == 0) break
                require(waitForTake > 0)

                val item = player.inventory.getItem(i)
                if (item == null || item.type == Material.AIR) {
                    val count = min(waitForTake, maxStackSize)
                    waitForTake -= count
                    player.inventory.setItem(i, info.origin.clone().apply { amount = count })
                    continue
                }
                if (!item.isSimilar(info.origin)) continue
                if (item.amount >= maxStackSize) continue
                val count = min(waitForTake, maxStackSize - item.amount)
                waitForTake -= count
                player.inventory.setItem(i, item.clone().apply { amount += count })
            }

            val totalTake = planTake - waitForTake
            RuntimeServices.messages.debug { "Player ${player.name} took ${info.origin.type}x${totalTake} from trashcan (remain: ${info.amount - totalTake})" }

            info.amount -= totalTake
            require(info.amount >= 0)

            if (info.amount == 0) {
                Trashcan.trashData.remove(info.origin.sign())
                Trashcan.trashValues.removeAt(itemIndex)
            }

            Trashcan.update()
            true
        },
    )

    val hasPrev get() = pager.hasPrev
    val hasNext get() = pager.hasNext
    val page get() = pager.page

    fun prevPage() = pager.prevPage()
    fun nextPage() = pager.nextPage()

    fun update() {
        if (pager.page != 0 && pager.page * 45 >= data.size) pager.prevPage()
        pager.render(menu.inventory)
    }

    fun onClickSelfInv(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        event.isCancelled = true
        val clicked = event.currentItem
        if (clicked == null || clicked.type == Material.AIR) return
        val count = when (event.click) {
            ClickType.LEFT, ClickType.DOUBLE_CLICK -> 1
            ClickType.SHIFT_LEFT -> clicked.amount
            ClickType.RIGHT -> max(clicked.amount / 2, 1)

            else -> {
                player.playSound(player.location, Sound.ENTITY_BLAZE_DEATH, 1F, 1F)
                return
            }
        }
        RuntimeServices.messages.debug { "Player ${player.name} deposited ${clicked.type}x${count} into trashcan (remain: ${clicked.amount - count})" }
        if (count == clicked.amount) {
            event.currentItem = emptyItem
            Trashcan.addItem(clicked)
            Trashcan.update()
            return
        }
        clicked.amount -= count
        event.currentItem = clicked
        Trashcan.addItem(clicked.clone().apply { amount = count })
        Trashcan.update()
    }

    fun onShiftPutin(clicked: ItemStack, event: InventoryClickEvent) {
        if (clicked.type == Material.AIR) return
        Trashcan.addItem(clicked)
        Trashcan.update()
        event.whoClicked.inventory.setItem(event.slot, emptyItem)
    }
}
