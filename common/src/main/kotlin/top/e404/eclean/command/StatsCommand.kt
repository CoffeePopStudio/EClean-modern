package top.e404.eclean.command

import top.e404.eclean.common.api.CommonCommandSender
import top.e404.eclean.common.api.CommonPlayer
import top.e404.eclean.feature.stats.StatsMenuService
import top.e404.eclean.feature.stats.WorldStatsProvider
import top.e404.eclean.feature.stats.WorldStatsResult
import top.e404.eclean.util.miniMessage
import top.e404.eclean.util.withColor

/**
 * Platform-agnostic `/eclean stats` command handler.
 */
fun statsCommandHandler(
    messageProvider: MessageProvider,
    worldStatsProvider: WorldStatsProvider,
    statsMenuService: StatsMenuService,
): (CommonCommandSender, Array<out String>) -> Boolean {
    fun execute(sender: CommonCommandSender, args: Array<out String>): Boolean {
        when {
            args.size == 1 -> {
                val player = sender as? CommonPlayer
                if (player == null) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.player_only")))
                    return true
                }
                if (!sender.hasPermission(Permissions.STATS_SELF)) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
                    return true
                }
                sendWorldStats(sender, player.worldName, messageProvider, worldStatsProvider)
            }
            args.size == 2 && args[1].equals("gui", true) -> {
                val player = sender as? CommonPlayer
                if (player == null) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.player_only")))
                    return true
                }
                if (!sender.hasPermission(Permissions.STATS_GUI)) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
                    return true
                }
                statsMenuService.openStatsGui(player, player.worldName)
            }
            args.size == 2 -> {
                if (!sender.hasPermission(Permissions.STATS_WORLD)) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
                    return true
                }
                sendWorldStats(sender, args[1], messageProvider, worldStatsProvider)
            }
            args.size == 3 && args[1].equals("gui", true) -> {
                val player = sender as? CommonPlayer
                if (player == null) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.player_only")))
                    return true
                }
                if (!sender.hasPermission(Permissions.STATS_GUI)) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
                    return true
                }
                statsMenuService.openStatsGui(player, args[2])
            }
            else -> {
                sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.usage.stats")))
            }
        }
        return true
    }
    return { sender, args -> execute(sender, args) }
}

private fun sendWorldStats(
    sender: CommonCommandSender,
    worldName: String,
    messageProvider: MessageProvider,
    worldStatsProvider: WorldStatsProvider,
) {
    worldStatsProvider.collectWorldStats(worldName) { result ->
        if (result == null) {
            sender.sendMessage(
                miniMessage.deserialize(messageProvider.get("command.invalid_world", "world" to worldName))
            )
            return@collectWorldStats
        }
        if (result.totalEntities == 0) {
            sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.stats.empty")))
            return@collectWorldStats
        }
        val entity = result.sortedEntries().joinToString(messageProvider.get("command.stats.spacing")) { (type, count) ->
            val command = "/eclean entity $type $worldName"
            val content = messageProvider.get("command.stats.content", "type" to type, "count" to count.withColor())
            "<click:run_command:'$command'><hover:show_text:'${messageProvider.get("common.hover.view_distribution")}'>$content</hover></click>"
        }
        sender.sendMessage(
            miniMessage.deserialize(
                messageProvider.get(
                    "command.stats.world",
                    "world" to worldName,
                    "count" to result.loadedChunks,
                    "force" to result.forceLoadedChunks,
                    "entity" to entity,
                )
            )
        )
    }
}
