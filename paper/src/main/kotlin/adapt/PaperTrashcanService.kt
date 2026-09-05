package top.e404.eclean.paper.adapt

import org.bukkit.Bukkit
import top.e404.eclean.common.api.CommonPlayer
import top.e404.eclean.config.Config
import top.e404.eclean.feature.trashcan.TrashcanEntryView
import top.e404.eclean.feature.trashcan.TrashcanManager
import top.e404.eclean.feature.trashcan.TrashcanService
import java.util.UUID

class PaperTrashcanService(
    private val manager: TrashcanManager,
) : TrashcanService {
    override val enabled: Boolean
        get() = Config.current.trashcan.enabled

    override fun open(player: CommonPlayer) {
        val bukkitPlayer = runCatching { UUID.fromString(player.uniqueId) }
            .getOrNull()
            ?.let { Bukkit.getPlayer(it) }
            ?: return
        manager.open(bukkitPlayer)
    }

    override fun entries(): List<TrashcanEntryView> =
        manager.stats().map {
            TrashcanEntryView(
                type = it.prototype.type.name,
                count = it.count,
                deadline = it.deadline,
            )
        }
}