package top.e404.eclean.menu.stats

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import top.e404.eclean.PL
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.UiDisplayable
import top.e404.eclean.ui.UiMenu
import top.e404.eclean.ui.UiPager
import top.e404.eclean.ui.buildItemStack

class StatsMenu(
    private val worldName: String,
    entries: List<Pair<EntityType, Int>>,
) : UiMenu(PL, MLang["command.stats_gui.title", "world" to worldName], 6, true) {

    private val data = entries.map { StatsEntry(it.first, it.second, worldName) }.toMutableList()

    private val pager = UiPager(
        data = data,
        pageSize = 45,
        startSlot = 0,
        onClickHandler = { index, event -> handleClick(index, event) },
    )

    init {
        addPager(pager)
    }

    private fun handleClick(index: Int, event: InventoryClickEvent): Boolean {
        val entry = data.getOrNull(index) ?: return true
        val player = event.whoClicked as? Player ?: return true
        player.performCommand("eclean entity ${entry.type.name} $worldName")
        return true
    }
}

private class StatsEntry(
    val type: EntityType,
    val count: Int,
    val world: String,
) : UiDisplayable {
    override var needUpdate = true
    override lateinit var item: ItemStack

    override fun update() {
        item = buildItemStack(
            Material.PAPER,
            1,
            MLang["command.stats_gui.item_name", "type" to type.name],
            MLang["command.stats_gui.item_lore", "world" to world, "count" to count].lines(),
        )
        needUpdate = false
    }
}
