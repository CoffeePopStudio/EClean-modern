package top.e404.eclean.menu.dense

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import top.e404.eclean.PL
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.UiButton
import top.e404.eclean.ui.UiMenu
import top.e404.eclean.ui.util.buildItemStack

class DenseMenu(data: MutableList<EntityInfo>) : UiMenu(PL, MLang["menu.dense.title"], 6, false) {
    val zone = DenseZone(this, data)
    var temp = false
    private val prev = PrevButton(this)
    private val next = NextButton(this)

    init {
        initSlots(
            listOf(
                "         ",
                "         ",
                "         ",
                "         ",
                "         ",
                "  p t n  ",
            )
        ) { char ->
            when (char) {
                'p' -> prev.button
                'n' -> next.button
                't' -> {
                    var item = createTempItem()
                    UiButton(
                        initialItem = item,
                        onClickHandler = { event ->
                            temp = !temp
                            val player = event.whoClicked as Player
                            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
                            updateIcon()
                            true
                        },
                        updateItemHandler = {
                            item = createTempItem()
                            it.setItem(item)
                        },
                    )
                }
                else -> null
            }
        }
        addPager(zone.pager)
    }

    private fun createTempItem() = buildItemStack(
        Material.PAPER,
        1,
        MLang["menu.dense.temp.name"],
        MLang["menu.dense.temp.lore", "status" to MLang["menu.dense.temp.status.$temp"]].lines(),
    ) {
        if (temp) {
            addEnchant(Enchantment.UNBREAKING, 1, true)
            addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
    }
}
