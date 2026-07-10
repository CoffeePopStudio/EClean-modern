package top.e404.eclean.menu.trashcan

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.UiButton
import top.e404.eclean.ui.buildItemStack
import top.e404.eclean.ui.emptyItem
import kotlin.math.max

class TrashcanNextButton(private val menu: TrashcanMenu) {
    private val btn = buildItemStack(
        Material.ARROW, 1,
        MLang["menu.trashcan.next.name"],
        MLang["menu.trashcan.next.lore"].lines(),
    )

    val button: UiButton = UiButton(
        initialItem = if (menu.hasNext) btn else emptyItem,
        onClickHandler = { event ->
            if (menu.hasNext) {
                val player = event.whoClicked as Player
                player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1F, 1F)
                menu.nextPage()
                menu.updateIcon()
            }
            true
        },
        updateItemHandler = { b ->
            b.setItem(
                if (!menu.hasNext) emptyItem
                else btn.clone().apply { amount = max(1, menu.currentPage + 2) }
            )
        },
    )
}
