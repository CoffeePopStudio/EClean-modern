package top.e404.eclean.command

import top.e404.eclean.common.api.CommonCommandSender
import top.e404.eclean.feature.cleanup.CleanupCommandService
import top.e404.eclean.util.miniMessage

/**
 * Platform-agnostic `/eclean clean` command handler.
 */
fun cleanCommandHandler(
    messageProvider: MessageProvider,
    cleanupService: CleanupCommandService,
): (CommonCommandSender, Array<out String>) -> Boolean {
    fun execute(sender: CommonCommandSender, args: Array<out String>): Boolean {
        val hasPreview = args.any { it.equals("--preview", true) }
        val cleanArgs = args.filter { !it.equals("--preview", true) }.toTypedArray()

        if (cleanArgs.isEmpty() || cleanArgs.size > 3) {
            sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.usage.clean")))
            return true
        }

        val target = when (cleanArgs.size) {
            1 -> CleanTarget.ALL
            2 -> when (cleanArgs[1].lowercase()) {
                "a", "all" -> CleanTarget.ALL
                "e", "entity" -> CleanTarget.ENTITY
                "d", "drop" -> CleanTarget.DROP
                "c", "chunk" -> CleanTarget.CHUNK
                "t", "trash" -> CleanTarget.TRASH
                else -> null
            }
            else -> when (cleanArgs[1].lowercase()) {
                "e", "entity" -> CleanTarget.ENTITY
                "d", "drop" -> CleanTarget.DROP
                "c", "chunk" -> CleanTarget.CHUNK
                else -> null
            }
        }
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.usage.clean")))
            return true
        }

        val permission = when (target) {
            CleanTarget.ALL -> Permissions.CLEAN_ALL
            CleanTarget.ENTITY -> Permissions.CLEAN_ENTITY
            CleanTarget.DROP -> Permissions.CLEAN_DROP
            CleanTarget.CHUNK -> Permissions.CLEAN_CHUNK
            CleanTarget.TRASH -> Permissions.CLEAN_TRASH
        }
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
            return true
        }
        if (hasPreview && !sender.hasPermission(Permissions.CLEAN_PREVIEW)) {
            sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
            return true
        }

        val worldName = cleanArgs.getOrNull(2)
        when (target) {
            CleanTarget.ALL -> cleanupService.cleanAll(sender, hasPreview)
            CleanTarget.ENTITY -> cleanupService.cleanEntity(sender, worldName, hasPreview)
            CleanTarget.DROP -> cleanupService.cleanDrop(sender, worldName, hasPreview)
            CleanTarget.CHUNK -> cleanupService.cleanChunk(sender, worldName, hasPreview)
            CleanTarget.TRASH -> cleanupService.cleanTrash(sender, hasPreview)
        }
        return true
    }
    return { sender, args -> execute(sender, args) }
}

private enum class CleanTarget {
    ALL, ENTITY, DROP, CHUNK, TRASH
}
