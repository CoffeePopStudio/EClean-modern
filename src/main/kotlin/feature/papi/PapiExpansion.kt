package top.e404.eclean.feature.papi

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eplugin.hook.placeholderapi.PapiExpansion
import top.e404.eclean.util.parseSecondAsDuration

open class ECleanPapiExpansion : PapiExpansion(PL, "eclean") {
    override fun onPlaceholderRequest(player: Player?, params: String) = onRequest(player, params)

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val snapshot = RuntimeServices.statusSnapshots.current()
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

    private val placeholders = mutableListOf(
        "%eclean_before_next%",
        "%eclean_before_next_formatted%",
        "%eclean_last_drop%",
        "%eclean_last_living%",
        "%eclean_last_chunk%",
        "%eclean_trashcan_countdown%",
        "%eclean_trashcan_countdown_formatted%",
    )

    override fun getPlaceholders() = placeholders
}
