package top.e404.eclean.command

import top.e404.eclean.common.api.CommonCommandSender
import top.e404.eclean.common.api.CommonPlayer
import top.e404.eclean.feature.stats.WorldStatsProvider
import top.e404.eclean.util.formatAsConst
import top.e404.eclean.util.miniMessage
import top.e404.eclean.util.withColor

/**
 * Platform-agnostic `/eclean entity` command handler.
 */
fun entityCommandHandler(
    messageProvider: MessageProvider,
    worldStatsProvider: WorldStatsProvider,
): (CommonCommandSender, Array<out String>) -> Boolean {
    fun execute(sender: CommonCommandSender, args: Array<out String>): Boolean {
        when (args.size) {
            2 -> {
                val player = sender as? CommonPlayer
                if (player == null) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.player_only")))
                    return true
                }
                if (!sender.hasPermission(Permissions.ENTITY_SELF)) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
                    return true
                }
                sendEntityStats(sender, player.worldName, args[1], 0, null, null, messageProvider, worldStatsProvider)
            }
            3 -> {
                if (!sender.hasPermission(Permissions.ENTITY_WORLD)) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
                    return true
                }
                sendEntityStats(sender, args[2], args[1], 0, null, null, messageProvider, worldStatsProvider)
            }
            4 -> {
                if (!sender.hasPermission(Permissions.ENTITY_WORLD)) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
                    return true
                }
                val min = args[3].toIntOrNull()
                if (min == null || min < 0) {
                    sender.sendMessage(
                        miniMessage.deserialize(messageProvider.get("message.invalid_number", "number" to args[3]))
                    )
                    return true
                }
                sendEntityStats(sender, args[2], args[1], min, null, null, messageProvider, worldStatsProvider)
            }
            5 -> {
                if (!sender.hasPermission(Permissions.ENTITY_CHUNK)) {
                    sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
                    return true
                }
                val chunkX = args[3].toIntOrNull()
                val chunkZ = args[4].toIntOrNull()
                if (chunkX == null || chunkZ == null) {
                    val invalid = if (chunkX == null) args[3] else args[4]
                    sender.sendMessage(
                        miniMessage.deserialize(messageProvider.get("message.invalid_number", "number" to invalid))
                    )
                    return true
                }
                sendEntityStats(
                    sender, args[2], args[1], 0, chunkX, chunkZ, messageProvider, worldStatsProvider
                )
            }
            else -> {
                sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.usage.entity")))
            }
        }
        return true
    }
    return { sender, args -> execute(sender, args) }
}

private fun sendEntityStats(
    sender: CommonCommandSender,
    worldName: String,
    typeName: String,
    min: Int,
    chunkX: Int?,
    chunkZ: Int?,
    messageProvider: MessageProvider,
    worldStatsProvider: WorldStatsProvider,
) {
    val type = typeName.formatAsConst()
    if (!worldStatsProvider.isValidEntityType(type)) {
        sender.sendMessage(miniMessage.deserialize(messageProvider.get("message.invalid_entity_type")))
        return
    }
    if (chunkX != null && chunkZ != null) {
        worldStatsProvider.collectChunkEntities(worldName, type, chunkX, chunkZ) { details ->
            if (details.isEmpty()) {
                sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.stats.empty")))
                return@collectChunkEntities
            }
            val entity = details.joinToString(messageProvider.get("command.stats.spacing")) { detail ->
                val command = "/eclean tp $worldName ${detail.x} ${detail.y} ${detail.z}"
                val hover = messageProvider.get("common.hover.tp")
                "<click:run_command:'$command'><hover:show_text:'$hover'><white>$typeName @ ${detail.x}, ${detail.y}, ${detail.z}</white></hover></click>"
            }
            sender.sendMessage(
                miniMessage.deserialize(
                    messageProvider.get("command.stats.entity", "type" to typeName, "entity" to entity)
                )
            )
        }
        return
    }
    worldStatsProvider.collectEntityStats(worldName, type, min) { entries ->
        if (entries.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.stats.empty")))
            return@collectEntityStats
        }
        val entity = entries.joinToString(messageProvider.get("command.stats.spacing")) { entry ->
            val command = "/eclean entity $typeName $worldName ${entry.chunkX} ${entry.chunkZ}"
            val label = "x: ${entry.chunkX * 16}..${entry.chunkX * 16 + 15}, z: ${entry.chunkZ * 16}..${entry.chunkZ * 16 + 15}"
            val hover = messageProvider.get("common.hover.view_chunk")
            val content = messageProvider.get("command.stats.content", "type" to label, "count" to entry.count.withColor())
            "<click:run_command:'$command'><hover:show_text:'$hover'>$content</hover></click>"
        }
        sender.sendMessage(
            miniMessage.deserialize(
                messageProvider.get("command.stats.entity", "type" to typeName, "entity" to entity)
            )
        )
    }
}
