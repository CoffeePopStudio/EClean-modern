package top.e404.eclean.feature.stats

import top.e404.eclean.common.api.MessageSender
import top.e404.eclean.common.api.PermissionService
import top.e404.eclean.common.api.ServerInfo
import top.e404.eclean.common.api.ScheduledTask
import top.e404.eclean.config.Config
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.util.miniMessage
import top.e404.eclean.util.placeholder

class StatsAlertService(
    private val serverInfo: ServerInfo,
    private val permissionService: PermissionService,
    private val messageSender: MessageSender,
    private val prefixProvider: () -> String,
) {
    private var task: ScheduledTask? = null

    fun start() {
        stop()
        val config = Config.current.cleanup
        if (!config.alertEnabled) return
        val intervalTicks = (config.alertCheckIntervalSeconds * 20).coerceAtLeast(20)
        task = Schedulers.scheduleRepeatingGlobal(intervalTicks, intervalTicks) {
            checkAlerts()
        }
    }

    fun stop() {
        task?.cancel()
        task = null
    }

    private fun checkAlerts() {
        val config = Config.current.cleanup
        if (!config.alertEnabled) return
        WorldStatsService().collectAllWorldStats { results ->
            results.forEach { (worldName, result) ->
                result.entityCounts
                    .filter { it.value >= config.alertEntityThreshold }
                    .forEach { (type, count) ->
                        val message = config.alertFormat.placeholder(
                            "world" to worldName,
                            "type" to type,
                            "count" to count,
                        )
                        val component = miniMessage.deserialize("${prefixProvider()} $message")
                        serverInfo.onlinePlayerIds.forEach { playerId ->
                            if (permissionService.hasPermission(playerId, "eclean.alerts")) {
                                messageSender.sendPlayer(playerId, component)
                            }
                        }
                    }
            }
        }
    }
}
