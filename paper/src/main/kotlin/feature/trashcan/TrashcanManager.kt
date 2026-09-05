package top.e404.eclean.feature.trashcan

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.e404.eclean.app.MessageService
import top.e404.eclean.command.PermissionNode
import top.e404.eclean.command.hasPermission
import top.e404.eclean.lang.MLang
import top.e404.eclean.menu.MenuManager
import top.e404.eclean.menu.trashcan.TrashcanMenu

class TrashcanManager(
    private val store: TrashcanItemStore,
    private val messages: MessageService,
) {
    fun open(player: Player) {
        MenuManager.openMenu(TrashcanMenu(store, this), player)
    }

    fun collectStacks(items: Collection<ItemStack>) {
        messages.debug { "收集 ${items.size} 组物品到垃圾桶" }
        store.addAll(items.map(::sanitizeItem))
        refreshOpenMenus()
    }

    fun addItem(item: ItemStack): Boolean {
        val accepted = store.addItem(sanitizeItem(item))
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

    internal fun refreshOpenMenus() {
        if (!MenuManager.hasOpenMenus()) return
        MenuManager.refreshTrashcanMenus()
    }

    private fun sanitizeItem(item: ItemStack): ItemStack {
        val meta = item.itemMeta ?: return item
        val lore = meta.lore() ?: return item
        val plain = PlainTextComponentSerializer.plainText()
        val filtered = lore.filter { line ->
            val text = plain.serialize(line)
            TRASH_LORE_PATTERNS.none { it.matches(text) }
        }
        if (filtered.size == lore.size) return item
        if (filtered.isEmpty()) meta.lore(null) else meta.lore(filtered)
        item.itemMeta = meta
        return item
    }

    private fun notifyAdmins(message: String) {
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(PermissionNode.ALERTS)) {
                messages.send(player, message)
            }
        }
    }

    private companion object {
        val TRASH_LORE_PATTERNS = listOf(
            Regex("^共\\d+个$"),
            Regex("^Total: \\d+$"),
            Regex("^剩余 .+"),
            Regex("^Expires in .+"),
        )
    }
}
