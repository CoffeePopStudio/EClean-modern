package top.e404.eclean.command
import top.e404.eclean.PL

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType
import top.e404.eclean.lang.MLang

object Commands : CommandExecutor, TabCompleter {
    private val subcommands = listOf("debug", "reload", "clean", "stats", "entity", "trash", "players", "show")

    fun register() {
        val cmd = Bukkit.getPluginCommand("eclean") ?: return
        cmd.setExecutor(this)
        cmd.tabCompleter = this
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sendUsage(sender)
            return true
        }
        when (args[0].lowercase()) {
            "d", "debug" -> DebugCommand.handle(sender)
            "r", "reload" -> ReloadCommand.handle(sender)
            "clean" -> CleanCommand.handle(sender, args)
            "s", "stats" -> StatsCommand.handle(sender, args)
            "e", "entity" -> EntityCommand.handle(sender, args)
            "t", "trash" -> TrashCommand.handle(sender, args)
            "p", "players" -> PlayersCommand.handle(sender)
            "show" -> ShowCommand.handle(sender)
            "tp" -> TeleportCommand.handle(sender, args)
            else -> sendUsage(sender)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        if (args.size == 1) return subcommands.filter { it.startsWith(args[0].lowercase()) }
        when (args[0].lowercase()) {
            "clean" -> {
                if (args.size == 2) return listOf("entity", "drop", "chunk", "trash").filter { it.startsWith(args[1].lowercase()) }
                if (args.size == 3) return Bukkit.getWorlds().map { it.name }.filter { it.startsWith(args[2].lowercase()) }
            }
            "s", "stats" -> {
                if (args.size == 2) return Bukkit.getWorlds().map { it.name }.filter { it.startsWith(args[1].lowercase()) }
            }
            "e", "entity" -> {
                if (args.size == 2) return EntityType.values().map { it.name }.filter { it.startsWith(args[1].uppercase()) }
                if (args.size == 3) return Bukkit.getWorlds().map { it.name }.filter { it.startsWith(args[2].lowercase()) }
            }
            "t", "trash" -> {
                if (args.size == 2) return listOf("stats").filter { it.startsWith(args[1].lowercase()) }
            }
        }
        return emptyList()
    }

    internal fun sendUsage(sender: CommandSender) {
        val keys = listOf(
            "command.usage.debug",
            "command.usage.reload",
            "command.usage.trash",
            "command.usage.players",
            "command.usage.show",
            "command.usage.stats",
            "command.usage.entity",
            "command.usage.clean",
        )
        for (key in keys) PL.services.messages.send(sender, MLang[key])
    }
}
