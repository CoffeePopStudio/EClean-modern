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
        return when (params.lowercase()) {
            "before_next" -> snapshot.cleanup.remainingSeconds.toString()
            "before_next_formatted" -> snapshot.cleanup.remainingSeconds.parseSecondAsDuration()
            "last_drop" -> snapshot.cleanup.lastDrop.toString()
            "last_living" -> snapshot.cleanup.lastLiving.toString()
            "last_chunk" -> snapshot.cleanup.lastChunk.toString()
            "trashcan_countdown" -> snapshot.trashcanCountdown.toString()
            "trashcan_countdown_formatted" -> snapshot.trashcanCountdown.parseSecondAsDuration()
            else -> null
        }
    }
}
