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
        Schedulers.runGlobal { TrashcanMenu.updateAllOpen() }
    }

    fun addItem(item: ItemStack): Boolean {
        val accepted = store.addItem(item)
        if (!accepted) {
            messages.debug { "垃圾桶已满，物品无法放入: ${item.type.name}" }
        }
        Schedulers.runGlobal { TrashcanMenu.updateAllOpen() }
        return accepted
    }

    fun clearAll() {
        if (store.isEmpty()) return
        messages.debug { "清空垃圾桶" }
        store.clear()
        notifyAdmins(MLang["command.trash_clean_done"])
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
