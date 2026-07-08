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
        internalSendDebug("&b$text".color)
    }

    fun buildDebug(block: StringBuilder.() -> Unit) {
        if (!plugin.debug && debuggers.isEmpty()) return
        internalSendDebug("&b${buildString(block)}".color)
    }

    fun info(message: String) {
        send(Bukkit.getConsoleSender(), "\u00a7f$message")
    }

    fun warn(message: String, throwable: Throwable? = null) {
        if (throwable == null) plugin.logger.log(Level.WARNING, message)
        else plugin.logger.log(Level.WARNING, message, throwable)
    }

    fun broadcast(message: String) {
        val stripped = message.removeColor()
        internalSendDebug(stripped)
        Bukkit.getOnlinePlayers().forEach { send(it, stripped) }
    }

    private fun internalSendDebug(message: String) {
        val formatted = "${plugin.debugPrefix} $message".color
        if (plugin.debug) Bukkit.getConsoleSender().sendMessage(formatted)
        debuggers.forEach { Bukkit.getPlayer(it)?.sendMessage(formatted) }
    }

    private fun String.removeColor(): String =
        replace(Regex("(?i)[§&][\\da-fk-orx]"), "")
}
