package top.e404.eclean.feature.trashcan

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.e404.eclean.app.MessageService
import top.e404.eclean.lang.MLang
import top.e404.eclean.menu.trashcan.TrashcanMenu
import top.e404.eclean.platform.Schedulers

class TrashcanManager(
    private val store: TrashcanItemStore,
    private val messages: MessageService,
) {
    fun open(player: Player) {
        TrashcanMenu(store, this).open(player)
    }

    fun collectStacks(items: Collection<ItemStack>) {
        messages.debug { "收集 ${items.size} 组物品到垃圾桶" }
        store.addAll(items)
        refreshOpenMenus()
    }

    fun addItem(item: ItemStack): Boolean {
        val accepted = store.addItem(item)
        refreshOpenMenus()
        return accepted
    }

    /** 垃圾桶条目快照(插入顺序), 供统计使用 */
    fun stats(): List<TrashcanEntry> = store.getEntries()

    fun clearAll() {
        if (store.isEmpty()) return
        messages.debug { "清空垃圾桶" }
        store.clear()
        notifyAdmins(MLang["command.trash_clean_done"])
        refreshOpenMenus()
    }

    private fun refreshOpenMenus() {
        if (TrashcanMenu.opened.isEmpty()) return
        Schedulers.runGlobal { TrashcanMenu.updateAllOpen() }
    }

    private fun notifyAdmins(message: String) {
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("eclean.admin")) {
                messages.send(player, message)
            }
        }
    }
}
