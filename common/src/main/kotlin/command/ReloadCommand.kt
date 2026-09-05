package top.e404.eclean.command

import top.e404.eclean.common.api.CommonCommandSender
import top.e404.eclean.util.miniMessage

/**
 * Platform-agnostic reload command handler.
 */
fun reloadCommandHandler(
    messageProvider: MessageProvider,
    onReload: () -> Unit,
): (CommonCommandSender, Array<out String>) -> Boolean = { sender, _ ->
    if (!sender.hasPermission(Permissions.RELOAD)) {
        sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
        true
    } else {
        onReload()
        sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.reload_done")))
        true
    }
}
