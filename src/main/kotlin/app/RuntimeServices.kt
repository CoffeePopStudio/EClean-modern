package top.e404.eclean.app

import org.bukkit.command.CommandSender
import top.e404.eclean.feature.cleanup.CleanupAnnouncementService
import top.e404.eclean.feature.cleanup.CleanupCoordinator
import top.e404.eclean.feature.cleanup.CleanupTickService
import top.e404.eclean.feature.trashcan.TrashcanItemStore
import top.e404.eclean.feature.trashcan.TrashcanManager
import top.e404.eclean.feature.trashcan.TrashcanTicker
import top.e404.eclean.config.Config
import top.e404.eclean.lang.MLang
import top.e404.eclean.platform.FoliaDetector
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.platform.execution.BukkitExecutionGateway
import top.e404.eclean.platform.execution.ExecutionGateway
import top.e404.eclean.platform.runtime.RuntimePlatform
import top.e404.eclean.platform.runtime.RuntimePlatformFactory
import top.e404.eclean.service.PlayerTeleportService
import top.e404.eclean.service.StatusSnapshotService
import top.e404.eclean.service.TemporaryReturnEvent
import top.e404.eclean.service.TemporaryReturnService
import top.e404.eclean.EClean

object RuntimeServices {
    lateinit var plugin: EClean
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

    lateinit var trashcanStore: TrashcanItemStore
        private set

    lateinit var trashcanManager: TrashcanManager
        private set

    lateinit var trashcanTicker: TrashcanTicker
        private set

    fun init(plugin: EClean) {
        this.plugin = plugin
        messages = MessageService()
        val isFolia = FoliaDetector.isFolia()
        platform = RuntimePlatformFactory.create(isFolia)
        execution = BukkitExecutionGateway()
        statusSnapshots = StatusSnapshotService()
        playerTeleportService = PlayerTeleportService(execution)
        temporaryReturnService = TemporaryReturnService(execution, playerTeleportService) { player, event ->
            val key = when (event) {
                TemporaryReturnEvent.Started -> "command.teleport.temp"
                TemporaryReturnEvent.Returned -> "command.teleport.back"
                TemporaryReturnEvent.ReturnedAfterReplace -> "command.teleport.cover"
            }
            messages.send(player, MLang[key])
        }
        trashcanStore = TrashcanItemStore(
            lifetimeSeconds = { Config.current.trashcan.clearIntervalSeconds },
            stackingEnabled = { Config.current.trashcan.stacking.enabled },
        )
        trashcanManager = TrashcanManager(trashcanStore, messages)
        trashcanTicker = TrashcanTicker(trashcanStore, statusSnapshots)
        cleanupAnnouncementService = CleanupAnnouncementService(messages, statusSnapshots)
        cleanupCoordinator = CleanupCoordinator(messages, statusSnapshots)
        cleanupTickService = CleanupTickService(messages, cleanupCoordinator, cleanupAnnouncementService, statusSnapshots)
    }

    fun load(sender: CommandSender? = null) {
        MLang.load(sender)
        Config.load(sender)
    }

    fun reload(sender: CommandSender) {
        Schedulers.runAsync {
            MLang.load(sender)
            Config.reload(sender)
            Schedulers.runGlobal {
                messages.send(sender, MLang["command.reload_done"])
            }
        }
    }

    fun shutdown() {
        if (::cleanupTickService.isInitialized) cleanupTickService.stop()
        if (::trashcanTicker.isInitialized) trashcanTicker.stop()
        if (::temporaryReturnService.isInitialized) temporaryReturnService.shutdown()
        Schedulers.cancelPluginTasks()
    }
}
