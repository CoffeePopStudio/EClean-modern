package top.e404.eclean.menu.dense

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import top.e404.eclean.PL
import top.e404.eclean.lang.MLang
import top.e404.eplugin.menu.menu.ChestMenu
import top.e404.eplugin.menu.slot.MenuButton
import top.e404.eplugin.util.buildItemStack

class DenseMenu(data: MutableList<EntityInfo>) : ChestMenu(PL, 6, MLang["menu.dense.title"], false) {
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
        ) { _, char ->
            when (char) {
                'p' -> prev
                'n' -> next
                't' -> object : MenuButton(this) {
                    private fun create() = buildItemStack(
                        Material.PAPER,
                        1,
                        MLang["menu.dense.temp.name"],
                        MLang["menu.dense.temp.lore", "status" to MLang["menu.dense.temp.status.$temp"]].lines()
                    ) {
                        if (temp) {
                            addEnchant(Enchantment.UNBREAKING, 1, true)
                            addItemFlags(ItemFlag.HIDE_ENCHANTS)
                        }
                    }

                    override var item = create()

                    override fun onClick(slot: Int, event: InventoryClickEvent): Boolean {
                        temp = !temp
                        val player = event.whoClicked as Player
                        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
                        menu.updateIcon()
                        return true
                    }

                    override fun updateItem() {
                        item = create()
                    }
                }

                else -> null
            }
        }
        zones.add(zone)
    }
}
