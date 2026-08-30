package top.e404.eclean.feature.papi.native
import top.e404.eclean.PL

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import top.e404.eclean.util.parseSecondAsDuration

class ECleanPapiExpansion : PlaceholderExpansion() {
    override fun getIdentifier(): String = "eclean"
    override fun getAuthor(): String = "404E"
    override fun getVersion(): String = "0.1.7"

    override fun canRegister(): Boolean =
        Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val snapshot = PL.services.statusSnapshots.current()
        val lower = params.lowercase()
        return when {
            lower == "before_next" -> snapshot.cleanup.remainingSeconds.toString()
            lower == "before_next_formatted" -> snapshot.cleanup.remainingSeconds.parseSecondAsDuration()
            lower == "last_drop" -> snapshot.cleanup.lastDrop.toString()
            lower == "last_living" -> snapshot.cleanup.lastLiving.toString()
            lower == "last_chunk" -> snapshot.cleanup.lastChunk.toString()
            lower == "trashcan_countdown" -> snapshot.trashcanCountdown.toString()
            lower == "trashcan_countdown_formatted" -> snapshot.trashcanCountdown.parseSecondAsDuration()
            lower == "total_entities" -> Bukkit.getWorlds().sumOf { it.entities.size }.toString()
            lower == "total_chunks" -> Bukkit.getWorlds().sumOf { it.loadedChunks.size }.toString()
            lower.startsWith("world_") && lower.endsWith("_entities") -> {
                val worldName = lower.removePrefix("world_").removeSuffix("_entities")
                Bukkit.getWorld(worldName)?.entities?.size?.toString()
            }
            else -> null
        }
    }
}
