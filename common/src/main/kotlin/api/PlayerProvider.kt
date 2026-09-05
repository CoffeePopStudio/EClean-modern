package top.e404.eclean.common.api

/**
 * Provides online players in a platform-agnostic way.
 */
interface PlayerProvider {
    fun onlinePlayers(): List<CommonPlayer>
}
