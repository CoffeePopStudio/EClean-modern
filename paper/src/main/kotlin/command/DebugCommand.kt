package top.e404.eclean.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.PL
import top.e404.eclean.config.Config
import java.util.UUID

object DebugCommand {
    fun handle(sender: CommandSender) {
        val common = if (sender is Player) sender.toCommonPlayer() else sender.toCommon()
        debugCommandHandler(
            messageProvider = PaperMessageProvider(),
            configProvider = { Config.current },
            configUpdater = { newBundle -> Config.update { newBundle } },
            togglePlayerDebugger = { playerId ->
                val player = runCatching { UUID.fromString(playerId) }
                    .getOrNull()
                    ?.let { Bukkit.getPlayer(it) }
                if (player == null) {
                    PL.services.messages.toggleDebugger(playerId)
                } else {
                    PL.services.messages.toggleDebugger(player.name)
                }
            }
        )(common, emptyArray())
    }
}
