package top.e404.eclean.common.api

/**
 * Top-level platform abstraction. Common code only talks to this interface;
 * each loader supplies its own implementation.
 */
interface Platform {
    val type: PlatformType
    val scheduler: Scheduler
    val messageSender: MessageSender
    val commandRegistry: CommandRegistry
    val permissionService: PermissionService
    val serverInfo: ServerInfo
    val worldAccess: WorldAccess
    val eventBus: EventBus
    val teleportService: TeleportService
    val playerProvider: PlayerProvider
    val trashcanService: top.e404.eclean.feature.trashcan.TrashcanService
    val worldStatsProvider: top.e404.eclean.feature.stats.WorldStatsProvider
    val denseShowService: top.e404.eclean.feature.cleanup.chunk.DenseShowService
    val statsMenuService: top.e404.eclean.feature.stats.StatsMenuService
    val cleanupCommandService: top.e404.eclean.feature.cleanup.CleanupCommandService
    fun shutdown()
}
