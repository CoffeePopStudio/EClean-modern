package top.e404.eclean.app

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import top.e404.eclean.lang.MLang
import java.util.logging.Level

class MessageService {
    private val miniMessage = MiniMessage.miniMessage()
    val debuggers = mutableSetOf<String>()

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
            Bukkit.broadcastMessage(stripMiniMessage(full))
        }
    }

    fun debug(msg: () -> String) {
        val hasDebuggers = debuggers.isNotEmpty()
        if (!hasDebuggers && !plugin.debug) return
        val text = msg()
        if (plugin.debug) {
            plugin.logger.info(stripMiniMessage("$debugPrefix $text"))
        }
        debuggers.forEach { Bukkit.getPlayer(it)?.sendMessage("$debugPrefix $text") }
    }

    fun buildDebug(block: StringBuilder.() -> Unit) {
        val hasDebuggers = debuggers.isNotEmpty()
        if (!hasDebuggers && !plugin.debug) return
        val text = buildString(block)
        if (plugin.debug) {
            plugin.logger.info(stripMiniMessage("$debugPrefix $text"))
        }
        debuggers.forEach { Bukkit.getPlayer(it)?.sendMessage("$debugPrefix $text") }
    }

    fun info(message: String) {
        plugin.logger.info(message)
    }

    fun warn(message: String, throwable: Throwable? = null) {
        if (throwable == null) plugin.logger.log(Level.WARNING, message)
        else plugin.logger.log(Level.WARNING, message, throwable)
    }

    private fun stripMiniMessage(text: String): String =
        text.replace(Regex("<[^>]+>"), "")

    private object plugin {
        val debug: Boolean get() = top.e404.eclean.config.Config.current.global.debug
        val logger get() = top.e404.eclean.PL.logger
    }
}
