package top.e404.eclean.app

import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import top.e404.eclean.lang.MLang
import top.e404.eclean.util.miniMessage
import java.util.logging.Level

class MessageService {
    val debuggers = mutableSetOf<String>()

    private val miniMessageTagRegex = Regex("<[^>]+>")

    val debugPrefix: String get() = MLang["debug_prefix"]
    val prefix: String get() = MLang["prefix"]

    fun send(sender: CommandSender, message: String) {
        val full = "$prefix $message"
        try {
            val component = miniMessage.deserialize(full)
            (sender as? Audience)?.sendMessage(component) ?: sender.sendMessage(stripMiniMessage(full))
        } catch (_: Exception) {
            sender.sendMessage(stripMiniMessage(full))
        }
    }

    fun broadcast(message: String) {
        val full = "$prefix $message"
        try {
            val component = miniMessage.deserialize(full)
            Bukkit.getServer().sendMessage(component)
        } catch (_: Exception) {
            Bukkit.getServer().broadcast(miniMessage.deserialize(stripMiniMessage(full)))
        }
    }

    fun debug(msg: () -> String) {
        if (!hasDebuggers && !plugin.debug) return
        emitDebug(msg())
    }

    fun buildDebug(block: StringBuilder.() -> Unit) {
        if (!hasDebuggers && !plugin.debug) return
        emitDebug(buildString(block))
    }

    fun info(message: String) {
        plugin.logger.info(message)
    }

    fun warn(message: String, throwable: Throwable? = null) {
        if (throwable == null) plugin.logger.log(Level.WARNING, message)
        else plugin.logger.log(Level.WARNING, message, throwable)
    }

    private val hasDebuggers: Boolean
        get() = debuggers.isNotEmpty()

    private fun emitDebug(text: String) {
        if (plugin.debug) {
            plugin.logger.info(stripMiniMessage("$debugPrefix $text"))
        }
        debuggers.forEach { Bukkit.getPlayer(it)?.sendMessage("$debugPrefix $text") }
    }

    private fun stripMiniMessage(text: String): String =
        text.replace(miniMessageTagRegex, "")

    private object plugin {
        val debug: Boolean get() = top.e404.eclean.config.Config.current.global.debug
        val logger get() = top.e404.eclean.PL.logger
    }
}
