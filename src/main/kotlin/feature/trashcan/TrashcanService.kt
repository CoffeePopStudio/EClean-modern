package top.e404.eclean.feature.trashcan

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.e404.eclean.lang.MLang
import top.e404.eclean.menu.trashcan.TrashcanMenu
import top.e404.eclean.service.StatusSnapshotService
import top.e404.eclean.app.MessageService

class TrashcanService(
    private val messages: MessageService,
    private val repository: TrashcanRepository,
    private val snapshots: StatusSnapshotService,
) {
    var countdown: Long = 0
        private set

    fun open(player: Player) {
        TrashcanMenu(repository).open(player)
    }

    fun collectStacks(items: Collection<ItemStack>) {
        repository.addAll(items)
        updateMenus()
    }

    fun addItem(item: ItemStack) {
        repository.addItem(item)
        updateMenus()
    }

    fun clearAll() {
        messages.debug { "清空垃圾桶" }
        repository.clear()
        messages.broadcast(MLang["command.trash_clean_done"])
        updateMenus()
    }

    fun syncCountdown(countdown: Long) {
        this.countdown = countdown
        snapshots.updateTrashcanCountdown(countdown)
    }

    fun updateMenus() {
        TrashcanMenu.updateAllOpen()
    }
}
