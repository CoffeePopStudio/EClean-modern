package top.e404.eclean.ui

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import kotlin.math.max

class PageButton(
    private val isNext: Boolean,
    private val hasPage: () -> Boolean,
    private val currentPage: () -> Int,
    private val pageAction: () -> Unit,
    private val refresh: () -> Unit,
    private val name: String,
    private val lore: List<String>,
) {
    private val btn = buildItemStack(
        Material.ARROW, 1,
        name,
        lore,
    )

    val button: UiButton = UiButton(
        initialItem = if (hasPage()) btn else emptyItem,
        onClickHandler = { event ->
            if (hasPage()) {
                val player = event.whoClicked as Player
                player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1F, 1F)
                pageAction()
                refresh()
            }
            true
        },
        updateItemHandler = { b ->
            b.setItem(
                if (!hasPage()) emptyItem
                else btn.clone().apply { amount = max(1, if (isNext) currentPage() + 2 else currentPage()) }
            )
        },
    )
}
