package top.e404.eclean.menu.trashcan

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import top.e404.eclean.PL
import top.e404.eclean.clean.Trashcan
import top.e404.eclean.command.PermissionNode
import top.e404.eclean.command.hasPermission
import top.e404.eclean.config.Config
import top.e404.eclean.feature.trashcan.TrashcanItemStore
import top.e404.eclean.feature.trashcan.TrashcanManager
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.PageButton
import top.e404.eclean.ui.UiButton
import top.e404.eclean.ui.UiMenu
import top.e404.eclean.ui.UiPager
import top.e404.eclean.ui.buildItemStack
import top.e404.eclean.ui.emptyItem

open class TrashcanMenu(
    private val store: TrashcanItemStore,
    private val manager: TrashcanManager,
) : UiMenu(PL, Config.current.advanced.menu.trashcanTitle ?: MLang["trash.title"], 6, true) {

    override fun isAllowed(player: Player): Boolean = player.hasPermission(PermissionNode.TRASH_OPEN)

    private var displayData = mutableListOf<TrashcanDisplayItem>()
    private var pager: UiPager<TrashcanDisplayItem>
    private var prevBtn: PageButton
    private var nextBtn: PageButton
    private var pagerInitialized = false

    private var category = TrashcanCategory.ALL
    private var sort = TrashcanSort.COUNT_DESC
    private var searchQuery: String? = null
    internal var isSearching = false
        private set

    val hasPrev get() = pager.hasPrev
    val hasNext get() = pager.hasNext
    val currentPage get() = pager.page

    fun prevPage() = pager.prevPage()
    fun nextPage() = pager.nextPage()

    init {
        sort = if (Config.current.trashcan.stacking.sortByCount) {
            TrashcanSort.COUNT_DESC
        } else {
            TrashcanSort.NAME_ASC
        }
        rebuildDisplayData()
        pager = UiPager(
            data = displayData,
            pageSize = ITEM_PAGE_SIZE,
            startSlot = 0,
            onClickHandler = { index, event -> handleItemClick(index, event) },
        )
        pagerInitialized = true
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

        val categoryBtn = createCategoryButton()
        val sortBtn = createSortButton()
        val searchBtn = createSearchButton()

        addPager(pager)

        initSlots(
            listOf(
                "         ",
                "         ",
                "         ",
                "         ",
                " c s r   ",
                "  p   n  ",
            )
        ) { char ->
            when (char) {
                'c' -> categoryBtn
                's' -> sortBtn
                'r' -> searchBtn
                'p' -> prevBtn.button
                'n' -> nextBtn.button
                else -> null
            }
        }
    }

    internal fun rebuildDisplayData() {
        val query = searchQuery
        val entries = store.getEntries()
            .filter { category == TrashcanCategory.ALL || category.matches(it.prototype.type) }
            .filter { entry -> query == null || entry.prototype.type.name.contains(query, true) }
        val sorted = when (sort) {
            TrashcanSort.COUNT_DESC -> entries.sortedByDescending { it.count }
            TrashcanSort.NAME_ASC -> entries.sortedBy { it.prototype.type.name }
            TrashcanSort.TIME_ASC -> entries.sortedBy { if (it.deadline == Long.MAX_VALUE) Long.MAX_VALUE else it.deadline }
        }
        displayData.clear()
        displayData.addAll(sorted.map { TrashcanDisplayItem(it) })
        if (pagerInitialized) pager.clampPage()
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

        val removed = store.removeItem(displayItem.prototype, placeable, displayItem.entry.id)
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

    private fun createCategoryButton(): UiButton {
        val item = buildItemStack(Material.HOPPER, 1, MLang["menu.trashcan.category.name"], null)
        return UiButton(
            initialItem = item,
            onClickHandler = {
                category = TrashcanCategory.entries[(category.ordinal + 1) % TrashcanCategory.entries.size]
                isSearching = false
                rebuildDisplayData()
                updateIcon()
                true
            },
            updateItemHandler = { btn ->
                val lore = MLang["menu.trashcan.category.${category.key}"].lines()
                btn.setItem(buildItemStack(Material.HOPPER, 1, MLang["menu.trashcan.category.name"], lore))
            },
        )
    }

    private fun createSortButton(): UiButton {
        val item = buildItemStack(Material.COMPARATOR, 1, MLang["menu.trashcan.sort.name"], null)
        return UiButton(
            initialItem = item,
            onClickHandler = {
                sort = TrashcanSort.entries[(sort.ordinal + 1) % TrashcanSort.entries.size]
                rebuildDisplayData()
                updateIcon()
                true
            },
            updateItemHandler = { btn ->
                val lore = MLang["menu.trashcan.sort.${sort.key}"].lines()
                btn.setItem(buildItemStack(Material.COMPARATOR, 1, MLang["menu.trashcan.sort.name"], lore))
            },
        )
    }

    private fun createSearchButton(): UiButton {
        val item = buildItemStack(Material.COMPASS, 1, MLang["menu.trashcan.search.name"], null)
        return UiButton(
            initialItem = item,
            onClickHandler = {
                if (searchQuery != null) {
                    searchQuery = null
                    isSearching = false
                } else {
                    isSearching = true
                    val player = inventory.viewers.firstOrNull() as? Player
                    if (player != null) {
                        PL.services.messages.send(player, MLang["menu.trashcan.search.prompt"])
                    }
                }
                rebuildDisplayData()
                updateIcon()
                true
            },
            updateItemHandler = { btn ->
                val lore = if (searchQuery != null) {
                    MLang["menu.trashcan.search.reset", "query" to searchQuery!!].lines()
                } else {
                    MLang["menu.trashcan.search.name"].lines()
                }
                btn.setItem(buildItemStack(Material.COMPASS, 1, MLang["menu.trashcan.search.name"], lore))
            },
        )
    }

    internal fun applySearchQuery(query: String?) {
        searchQuery = query?.takeIf { it.isNotBlank() }
        isSearching = false
        rebuildDisplayData()
        updateIcon()
    }

    companion object {
        private const val ITEM_PAGE_SIZE = 45
        private const val PLAYER_INV_SIZE = 36
    }
}