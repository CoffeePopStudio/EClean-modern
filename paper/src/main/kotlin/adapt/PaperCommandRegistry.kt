package top.e404.eclean.paper.adapt

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import top.e404.eclean.common.api.CommonCommand
import top.e404.eclean.common.api.CommonCommandSender
import top.e404.eclean.common.api.CommandRegistry

class PaperCommandRegistry(
    private val plugin: Plugin,
) : CommandRegistry {

    override fun register(command: CommonCommand) {
        val existing = plugin.server.getPluginCommand(command.name)
        if (existing != null) {
            existing.setExecutor { sender, _, _, args ->
                command.handler(sender.adapt(), args)
            }
            return
        }

        val bukkitCommand = object : org.bukkit.command.Command(command.name) {
            init {
                aliases = command.aliases
                permission = command.permission
            }

            override fun execute(
                sender: CommandSender,
                commandLabel: String,
                args: Array<out String>,
            ): Boolean = command.handler(sender.adapt(), args)

            override fun tabComplete(
                sender: CommandSender,
                alias: String,
                args: Array<out String>,
            ): MutableList<String> = mutableListOf()
        }
        plugin.server.commandMap.register(plugin.name.lowercase(), bukkitCommand)
    }

    override fun unregister(command: CommonCommand) {
        plugin.server.getPluginCommand(command.name)?.let {
            it.setExecutor(null)
            it.unregister(plugin.server.commandMap)
        }
    }
}

private fun CommandSender.adapt(): CommonCommandSender = object : CommonCommandSender {
    override val name: String = this@adapt.name

    override fun hasPermission(node: String): Boolean = this@adapt.hasPermission(node)

    override fun sendMessage(component: Component) {
        (this@adapt as? Audience)?.sendMessage(component)
            ?: this@adapt.sendMessage(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component))
    }
}
