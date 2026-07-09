package top.e404.eclean.app

import org.bukkit.command.CommandSender
import top.e404.eclean.feature.cleanup.CleanupAnnouncementService
import top.e404.eclean.feature.cleanup.CleanupCoordinator
import top.e404.eclean.feature.cleanup.CleanupTickService
import top.e404.eclean.feature.trashcan.TrashcanRepository
import top.e404.eclean.feature.trashcan.TrashcanService
import top.e404.eclean.feature.trashcan.TrashcanTicker
import top.e404.eclean.config.Config
import top.e404.eclean.config.Lang
import top.e404.eclean.platform.FoliaDetector
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.platform.execution.ExecutionGateway
import top.e404.eclean.platform.execution.FoliaExecutionGateway
import top.e404.eclean.platform.execution.PaperExecutionGateway
import top.e404.eclean.platform.runtime.RuntimePlatform
import top.e404.eclean.platform.runtime.RuntimePlatformFactory
import top.e404.eclean.service.PlayerTeleportService
import top.e404.eclean.service.StatusSnapshotService
import top.e404.eclean.service.TemporaryReturnEvent
import top.e404.eclean.service.TemporaryReturnService
import top.e404.eplugin.EPlugin

object RuntimeServices {
    lateinit var plugin: EPlugin
        private set

    lateinit var messages: MessageService
        private set

    lateinit var platform: RuntimePlatform
        private set

    lateinit var execution: ExecutionGateway
        private set

    lateinit var statusSnapshots: StatusSnapshotService
        private set

    lateinit var playerTeleportService: PlayerTeleportService
        private set

    lateinit var temporaryReturnService: TemporaryReturnService
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
        messages = MessageService()
        val isFolia = FoliaDetector.isFolia()
        platform = RuntimePlatformFactory.create(isFolia)
        execution = if (isFolia) FoliaExecutionGateway() else PaperExecutionGateway()
        statusSnapshots = StatusSnapshotService()
        playerTeleportService = PlayerTeleportService(execution)
        temporaryReturnService = TemporaryReturnService(execution, playerTeleportService) { player, event ->
            val key = when (event) {
                TemporaryReturnEvent.Started -> "command.teleport.temp"
                TemporaryReturnEvent.Returned -> "command.teleport.back"
                TemporaryReturnEvent.ReturnedAfterReplace -> "command.teleport.cover"
            }
            messages.send(player, Lang[key])
        }
        trashcanRepository = TrashcanRepository()
        trashcanService = TrashcanService(messages, trashcanRepository, statusSnapshots)
        trashcanTicker = TrashcanTicker(trashcanService, statusSnapshots)
        cleanupAnnouncementService = CleanupAnnouncementService(messages, statusSnapshots)
        cleanupCoordinator = CleanupCoordinator(messages, statusSnapshots)
        cleanupTickService = CleanupTickService(messages, cleanupCoordinator, cleanupAnnouncementService, statusSnapshots)
    }

    fun load(sender: CommandSender? = null) {
        Lang.load(sender)
        Config.load(sender)
    }

    fun reload(sender: CommandSender) {
        Schedulers.runAsync {
            Lang.load(sender)
            Config.reload(sender)
            Schedulers.runGlobal {
                messages.send(sender, Lang["command.reload_done"])
            }
        }
    }

    fun applyRuntimeConfig() {
        if (!::cleanupTickService.isInitialized || !::trashcanTicker.isInitialized) return
        cleanupTickService.start()
        trashcanTicker.start()
    }

    fun shutdown() {
        if (::cleanupTickService.isInitialized) cleanupTickService.stop()
        if (::trashcanTicker.isInitialized) trashcanTicker.stop()
        if (::temporaryReturnService.isInitialized) temporaryReturnService.shutdown()
        Schedulers.cancelPluginTasks()
    }
}
