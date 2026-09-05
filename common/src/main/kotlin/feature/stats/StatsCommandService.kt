package top.e404.eclean.feature.stats

import top.e404.eclean.common.api.CommonCommandSender
import top.e404.eclean.common.api.CommonPlayer

/**
 * Backs `/eclean stats` output and GUI actions.
 */
interface StatsCommandService {
    fun sendWorldStats(sender: CommonCommandSender, worldName: String)
    fun openStatsGui(player: CommonPlayer, worldName: String)
}
