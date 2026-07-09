package top.e404.eclean.menu.trashcan

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.UiButton
import kotlin.math.max

class PrevButton(viewMenu: TrashcanMenu) {
    val zone = viewMenu.zone
    private val btn = buildItemStack(
        Material.ARROW, 1,
        MLang["menu.trashcan.prev.name"],
        MLang["menu.trashcan.prev.lore"].lines(),
    )

    val button: UiButton = UiButton(
        initialItem = if (zone.hasPrev) btn else emptyItem,
        onClickHandler = { event ->
            if (zone.hasPrev) {
                val player = event.whoClicked as Player
                player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1F, 1F)
                zone.prevPage()
                viewMenu.updateIcon()
            }
            true
        },
        updateItemHandler = { b ->
            b.setItem(
                if (!zone.hasPrev) emptyItem
                else btn.clone().apply { amount = max(1, zone.page) }
            )
        },
    )
}
