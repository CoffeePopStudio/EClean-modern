package top.e404.eclean.command

import top.e404.eclean.common.api.CommonCommandSender
import top.e404.eclean.common.api.CommonPlayer
import top.e404.eclean.feature.cleanup.chunk.DenseShowService
import top.e404.eclean.util.miniMessage

/**
 * Platform-agnostic `/eclean show` command handler.
 */
fun showCommandHandler(
    messageProvider: MessageProvider,
    denseShowService: DenseShowService,
): (CommonCommandSender, Array<out String>) -> Boolean = { sender, _ ->
    if (!sender.hasPermission(Permissions.SHOW)) {
        sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.no_permission")))
        true
    } else {
        val player = sender as? CommonPlayer
        if (player == null) {
            sender.sendMessage(miniMessage.deserialize(messageProvider.get("command.player_only")))
            true
        } else {
            denseShowService.show(player)
            true
        }
    }
}
