package top.e404.eclean.common.api

/**
 * Platform-agnostic player teleport service.
 */
interface TeleportService {
    fun teleport(player: CommonPlayer, target: CommonLocation)
}
