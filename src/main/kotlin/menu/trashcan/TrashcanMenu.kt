package top.e404.eclean.menu.trashcan

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import top.e404.eclean.PL
import top.e404.eclean.clean.Trashcan
import top.e404.eclean.config.Config
import top.e404.eclean.feature.trashcan.TrashcanItemStore
import top.e404.eclean.feature.trashcan.TrashcanManager
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.PageButton
import top.e404.eclean.ui.UiMenu
import top.e404.eclean.ui.UiPager
import top.e404.eclean.ui.emptyItem

open class TrashcanMenu(
    private val store: TrashcanItemStore,
    private val manager: TrashcanManager,
) : UiMenu(PL, MLang["trash.title"], 6, false) {

    private var displayData = mutableListOf<TrashcanDisplayItem>()
    private var pager: UiPager<TrashcanDisplayItem>
    private var prevBtn: PageButton
    private var nextBtn: PageButton

    val hasPrev get() = pager.hasPrev
    val hasNext get() = pager.hasNext
    val currentPage get() = pager.page

    fun prevPage() = pager.prevPage()
    fun nextPage() = pager.nextPage()

    init {
        rebuildDisplayData()
        pager = UiPager(
            data = displayData,
            pageSize = ITEM_PAGE_SIZE,
            startSlot = 0,
            onClickHandler = { index, event -> handleItemClick(index, event) },
        )
        prevBtn = PageButton(
            isNext = false,
            hasPage = { hasPrev },
            currentPage = { currentPage },
            pageAction = { prevPage() },
            refresh = { updateIcon() },
            name = MLang["menu.trashcan.prev.name"],
            lore = MLang["menu.trashcan.prev.lore"].lines(),
        )
        nextBtn = PageButton(
            isNext = true,
            hasPage = { hasNext },
            currentPage = { currentPage },
            pageAction = { nextPage() },
            refresh = { updateIcon() },
            name = MLang["menu.trashcan.next.name"],
            lore = MLang["menu.trashcan.next.lore"].lines(),
        )
        addPager(pager)

        initSlots(
            listOf(
                "         ",
                "         ",
                "         ",
                "         ",
                "         ",
                "  p   n  ",
            )
        ) { char ->
            when (char) {
                'p' -> prevBtn.button
                'n' -> nextBtn.button
                else -> null
            }
        }
    }

    internal fun rebuildDisplayData() {
        val stacking = Config.current.trashcan.stacking
        val entries = store.getEntries()
        val sorted = if (stacking.sortByCount) entries.sortedByDescending { it.count } else entries
        displayData.clear()
        displayData.addAll(sorted.map { TrashcanDisplayItem(it) })
    }

    override fun open(player: Player) {
        rebuildDisplayData()
        super.open(player)
    }

    override fun handlePlayerInvClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val clicked = event.currentItem
        if (clicked == null || clicked.type == Material.AIR) return

        event.isCancelled = true

        val count = when (event.click) {
            ClickType.LEFT, ClickType.DOUBLE_CLICK -> 1
            ClickType.SHIFT_LEFT -> clicked.amount
            ClickType.RIGHT -> maxOf(clicked.amount / 2, 1)
            else -> return
        }

        if (count == clicked.amount) {
            event.currentItem = emptyItem
            Trashcan.addItem(clicked)
        } else {
            clicked.amount -= count
            event.currentItem = clicked
            Trashcan.addItem(clicked.clone().apply { amount = count })
        }

        rebuildDisplayData()
        updateIcon()
    }

    private fun handleItemClick(index: Int, event: InventoryClickEvent): Boolean {
        val displayItem = displayData.getOrNull(index) ?: return false
        val player = event.whoClicked as? Player ?: return false

        val take = when (event.click) {
            ClickType.LEFT, ClickType.DOUBLE_CLICK -> 1
            ClickType.SHIFT_LEFT -> displayItem.stackType.maxStackSize
            ClickType.RIGHT -> maxOf(displayItem.stackType.maxStackSize / 2, 1)
            else -> return false
        }

        // 先计算背包最多能放多少，再按这个数量从垃圾桶扣除，最后发放；
        // 避免“先发物品、后扣库存”在库存不足/菜单过期时复制物品。
        val maxStackSize = displayItem.stackType.maxStackSize
        var placeable = 0
        var remainingToSimulate = take
        for (i in 0 until PLAYER_INV_SIZE) {
            if (remainingToSimulate == 0) break
            val slotItem = player.inventory.getItem(i)
            if (slotItem == null || slotItem.type == Material.AIR) {
                val count = minOf(remainingToSimulate, maxStackSize)
                remainingToSimulate -= count
                placeable += count
            } else if (slotItem.isSimilar(displayItem.prototype) && slotItem.amount < maxStackSize) {
                val count = minOf(remainingToSimulate, maxStackSize - slotItem.amount)
                remainingToSimulate -= count
                placeable += count
            }
        }

        val removed = store.removeItem(displayItem.prototype, placeable)
        if (removed <= 0) {
            manager.refreshOpenMenus()
            return true
        }

        var remainingToPlace = removed
        for (i in 0 until PLAYER_INV_SIZE) {
            if (remainingToPlace == 0) break
            val slotItem = player.inventory.getItem(i)
            if (slotItem == null || slotItem.type == Material.AIR) {
                val count = minOf(remainingToPlace, maxStackSize)
                remainingToPlace -= count
                player.inventory.setItem(i, displayItem.prototype.clone().apply { amount = count })
                continue
            }
            if (!slotItem.isSimilar(displayItem.prototype)) continue
            if (slotItem.amount >= maxStackSize) continue
            val count = minOf(remainingToPlace, maxStackSize - slotItem.amount)
            remainingToPlace -= count
            player.inventory.setItem(i, slotItem.clone().apply { amount += count })
        }

        // 正常情况下不会走到这里；万一有极端并发，把没放下的部分放回垃圾桶
        if (remainingToPlace > 0) {
            store.addItem(displayItem.prototype.clone().apply { amount = remainingToPlace })
        }

        manager.refreshOpenMenus()
        return true
    }

    companion object {
        private const val ITEM_PAGE_SIZE = 45
        private const val PLAYER_INV_SIZE = 36
    }
}