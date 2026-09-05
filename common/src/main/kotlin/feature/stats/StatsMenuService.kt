package top.e404.eclean.feature.stats

import top.e404.eclean.common.api.CommonPlayer

/**
 * Opens the world statistics GUI for a player.
 */
interface StatsMenuService {
    fun openStatsGui(player: CommonPlayer, worldName: String)
}
