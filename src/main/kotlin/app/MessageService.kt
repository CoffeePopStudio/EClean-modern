package top.e404.eclean.app

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import top.e404.eclean.util.color
import top.e404.eplugin.EPlugin
import java.util.logging.Level

class MessageService(private val plugin: EPlugin) {
    val debuggers get() = plugin.debuggers

    fun send(sender: CommandSender, message: String) {
        sender.sendMessage("${plugin.prefix} $message".color)
    }

    fun debug(msg: () -> String) {
        if (!plugin.debug && debuggers.isEmpty()) return
        val text = msg()
        val formatted = "${plugin.debugPrefix} &b$text".color
        if (plugin.debug) plugin.logger.info(removeColor(formatted))
        debuggers.forEach { Bukkit.getPlayer(it)?.sendMessage(formatted) }
    }

    fun buildDebug(block: StringBuilder.() -> Unit) {
        if (!plugin.debug && debuggers.isEmpty()) return
        val formatted = "${plugin.debugPrefix} &b${buildString(block)}".color
        if (plugin.debug) plugin.logger.info(removeColor(formatted))
        debuggers.forEach { Bukkit.getPlayer(it)?.sendMessage(formatted) }
    }

    fun info(message: String) {
        plugin.logger.info(removeColor(message))
    }

    fun warn(message: String, throwable: Throwable? = null) {
        if (throwable == null) plugin.logger.log(Level.WARNING, message)
        else plugin.logger.log(Level.WARNING, message, throwable)
    }

    fun broadcast(message: String) {
        val stripped = removeColor(message)
        Bukkit.getOnlinePlayers().forEach { send(it, stripped) }
    }

    private fun removeColor(text: String): String =
        text.replace(Regex("(?i)[§&][\\da-fk-orx]"), "")
}
