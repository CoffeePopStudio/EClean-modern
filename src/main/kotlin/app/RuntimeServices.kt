package top.e404.eclean.app

import top.e404.eclean.feature.cleanup.CleanupAnnouncementService
import top.e404.eclean.feature.cleanup.CleanupCoordinator
import top.e404.eclean.feature.cleanup.CleanupTickService
import top.e404.eclean.feature.trashcan.TrashcanRepository
import top.e404.eclean.feature.trashcan.TrashcanService
import top.e404.eclean.feature.trashcan.TrashcanTicker
import top.e404.eclean.platform.FoliaDetector
import top.e404.eclean.platform.FoliaSchedulerFacade
import top.e404.eclean.platform.PaperSchedulerFacade
import top.e404.eclean.platform.SchedulerFacade
import top.e404.eclean.service.StatusSnapshotService
import top.e404.eplugin.EPlugin

object RuntimeServices {
    lateinit var plugin: EPlugin
        private set

    lateinit var scheduler: SchedulerFacade
        private set

    lateinit var statusSnapshots: StatusSnapshotService
        private set

    lateinit var cleanupAnnouncementService: CleanupAnnouncementService
        private set

    lateinit var cleanupCoordinator: CleanupCoordinator
        private set

    lateinit var cleanupTickService: CleanupTickService
        private set

    lateinit var trashcanRepository: TrashcanRepository
        private set

    lateinit var trashcanService: TrashcanService
        private set

    lateinit var trashcanTicker: TrashcanTicker
        private set

    fun init(plugin: EPlugin) {
        this.plugin = plugin
        scheduler = if (FoliaDetector.isFolia()) FoliaSchedulerFacade(plugin) else PaperSchedulerFacade(plugin)
        statusSnapshots = StatusSnapshotService()
        trashcanRepository = TrashcanRepository()
        trashcanService = TrashcanService(plugin, scheduler, trashcanRepository, statusSnapshots)
        trashcanTicker = TrashcanTicker(scheduler, trashcanService, statusSnapshots)
        cleanupAnnouncementService = CleanupAnnouncementService(plugin, statusSnapshots)
        cleanupCoordinator = CleanupCoordinator(plugin, scheduler, cleanupAnnouncementService, statusSnapshots)
        cleanupTickService = CleanupTickService(plugin, scheduler, cleanupCoordinator, cleanupAnnouncementService, statusSnapshots)
    }
}
