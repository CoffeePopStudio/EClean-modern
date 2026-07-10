package top.e404.eclean.menu.trashcan

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import top.e404.eclean.PL
import top.e404.eclean.clean.Trashcan
import top.e404.eclean.feature.trashcan.TrashcanItemStore
import top.e404.eclean.feature.trashcan.TrashcanManager
import top.e404.eclean.lang.MLang
import top.e404.eclean.ui.UiMenu
import top.e404.eclean.ui.UiPager
import top.e404.eclean.ui.emptyItem

open class TrashcanMenu(
    private val store: TrashcanItemStore,
    private val manager: TrashcanManager,
) : UiMenu(PL, MLang["trash.title"], 6, false) {

    private var displayData = mutableListOf<TrashcanDisplayItem>()
    private var pager: UiPager<TrashcanDisplayItem>
    private var prevBtn: TrashcanPrevButton
    private var nextBtn: TrashcanNextButton

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
        prevBtn = TrashcanPrevButton(this)
        nextBtn = TrashcanNextButton(this)
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

    private fun rebuildDisplayData() {
        val snapshot = store.getSnapshot()
        displayData.clear()
        displayData.addAll(snapshot.map { TrashcanDisplayItem(it) })
    }

    override fun open(player: Player) {
        rebuildDisplayData()
        super.open(player)
        opened.add(this)
        ensureCloseListener()
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
            ClickType.SHIFT_LEFT -> displayItem.item.maxStackSize
            ClickType.RIGHT -> maxOf(displayItem.item.maxStackSize / 2, 1)
            else -> return false
        }

        var waitForPut = take
        val maxStackSize = displayItem.stackType.maxStackSize
        for (i in 0 until PLAYER_INV_SIZE) {
            if (waitForPut == 0) break
            val slotItem = player.inventory.getItem(i)
            if (slotItem == null || slotItem.type == Material.AIR) {
                val count = minOf(waitForPut, maxStackSize)
                waitForPut -= count
                player.inventory.setItem(i, displayItem.snapshot.clone().apply { amount = count })
                continue
            }
            if (!slotItem.isSimilar(displayItem.snapshot)) continue
            if (slotItem.amount >= maxStackSize) continue
            val count = minOf(waitForPut, maxStackSize - slotItem.amount)
            waitForPut -= count
            player.inventory.setItem(i, slotItem.clone().apply { amount += count })
        }
        val given = take - waitForPut
        if (given <= 0) return true

        val matchItem = store.getSnapshot().firstOrNull { it.isSimilar(displayItem.snapshot) } ?: run {
            rebuildDisplayData()
            updateIcon()
            return true
        }
        store.removeItem(matchItem, given)
        rebuildDisplayData()
        updateIcon()
        return true
    }

    protected fun saveAndClose() {
        unregister()
        opened.remove(this)
    }

    companion object {
        private const val ITEM_PAGE_SIZE = 45
        private const val PLAYER_INV_SIZE = 36

        val opened = mutableSetOf<TrashcanMenu>()
        private var closeListenerRegistered = false

        private fun ensureCloseListener() {
            if (closeListenerRegistered) return
            closeListenerRegistered = true
            PL.server.pluginManager.registerEvents(object : Listener {
                @EventHandler
                fun onClose(event: InventoryCloseEvent) {
                    opened.find { it.inventory == event.inventory }?.saveAndClose()
                }
            }, PL)
        }

        fun updateAllOpen() {
            opened.toSet().forEach {
                it.rebuildDisplayData()
                it.updateIcon()
            }
        }
    }
}