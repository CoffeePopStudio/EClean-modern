package top.e404.eclean.feature.cleanup.chunk

import org.bukkit.Bukkit
import top.e404.eclean.PL
import top.e404.eclean.config.Config
import top.e404.eplugin.EPlugin.Companion.placeholder

class ChunkAlertService {
    fun alert(entries: List<ChunkDensityEntry>) {
        val format = Config.current.chunkDensity.alertFormat ?: return
        if (format.isBlank()) return
        val receivers = Bukkit.getOnlinePlayers().filter { it.hasPermission("eclean.admin") }
        entries.forEach { entry ->
            val message = format.placeholder(
                "chunk" to "x: ${entry.chunk.x * 16}..${entry.chunk.x * 16 + 15}, z: ${entry.chunk.z * 16}..${entry.chunk.z * 16 + 15}",
                "entity" to entry.entityType.name,
                "count" to entry.amount,
            )
            receivers.forEach { PL.sendMsgWithPrefix(it, message) }
        }
    }
}
