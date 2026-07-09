package top.e404.eclean.command

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.clean.Clean
import top.e404.eclean.clean.Trashcan.cleanTrash
import top.e404.eclean.clean.cleanDenseEntities
import top.e404.eclean.clean.cleanDrop
import top.e404.eclean.clean.cleanLiving
import top.e404.eclean.config.Config
import top.e404.eclean.feature.cleanup.chunk.ChunkDensityScanner
import top.e404.eclean.feature.cleanup.drop.DropCleanupService
import top.e404.eclean.feature.cleanup.living.LivingCleanupService
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
            "d", "debug" -> handleDebug(sender)
            "r", "reload" -> handleReload(sender)
            "clean" -> handleClean(sender, args)
            "s", "stats" -> handleStats(sender, args)
            "e", "entity" -> handleEntity(sender, args)
            "t", "trash" -> handleTrash(sender)
            "p", "players" -> handlePlayers(sender)
            "show" -> handleShow(sender)
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
                if (args.size == 2) return EntityType.entries.map { it.name }.filter { it.startsWith(args[1].uppercase()) }
                if (args.size == 3) return Bukkit.getWorlds().map { it.name }.filter { it.startsWith(args[2].lowercase()) }
            }
        }
        return emptyList()
    }

    private fun sendUsage(sender: CommandSender) {
        sender.sendMessage(MLang["command.usage.stats"])
    }

    private fun handleDebug(sender: CommandSender) {
        if (!sender.hasPermission("eclean.admin")) return
        if (sender !is Player) {
            if (Config.current.global.debug) {
                Config.update { it.copy(global = it.global.copy(debug = false)) }
                RuntimeServices.messages.send(sender, MLang["debug.console_disable"])
            } else {
                Config.update { it.copy(global = it.global.copy(debug = true)) }
                RuntimeServices.messages.send(sender, MLang["debug.console_enable"])
            }
            return
        }
        val senderName = sender.name
        if (senderName in RuntimeServices.messages.debuggers) {
            RuntimeServices.messages.debuggers.remove(senderName)
            RuntimeServices.messages.send(sender, MLang["debug.player_disable"])
        } else {
            RuntimeServices.messages.debuggers.add(senderName)
            RuntimeServices.messages.send(sender, MLang["debug.player_enable"])
        }
    }

    private fun handleReload(sender: CommandSender) {
        RuntimeServices.reload(sender)
    }

    private fun handleClean(sender: CommandSender, args: Array<out String>) {
        val dryRun = args.contains("--preview")
        val filteredArgs = args.filter { it != "--preview" }.toTypedArray()

        when (filteredArgs.size) {
            1 -> RuntimeServices.cleanupCoordinator.cleanNow(dryRun = dryRun) {
                if (dryRun) RuntimeServices.messages.send(sender, "&aPreview complete — no entities were removed")
            }
            2 -> when (filteredArgs[1].lowercase()) {
                "e", "entity" -> {
                    if (dryRun) {
                        LivingCleanupService().cleanAllWorlds(dryRun = true) { results ->
                            val total = results.sumOf { it.total }
                            val cleaned = results.sumOf { it.cleaned }
                            RuntimeServices.messages.send(sender, "&aPreview — Living: &6$cleaned&a/$total entities across &6${results.size}&a worlds")
                        }
                    } else { cleanLiving() }
                }
                "d", "drop" -> {
                    if (dryRun) {
                        DropCleanupService().cleanAllWorlds(dryRun = true) { results ->
                            val total = results.sumOf { it.total }
                            val cleaned = results.sumOf { it.cleaned }
                            RuntimeServices.messages.send(sender, "&aPreview — Drop: &6$cleaned&a/$total items across &6${results.size}&a worlds")
                        }
                    } else { cleanDrop() }
                }
                "c", "chunk" -> {
                    if (dryRun) {
                        ChunkDensityScanner().cleanAllWorlds(dryRun = true) { result ->
                            RuntimeServices.messages.send(sender, "&aPreview — Chunk density: &6${result.cleaned}&a entities across &6${result.denseEntries.size}&a dense regions")
                        }
                    } else { cleanDenseEntities() }
                }
                "t", "trash" -> cleanTrash()
                else -> sendUsage(sender)
            }
            3 -> {
                val worldName = filteredArgs[2]
                when (filteredArgs[1].lowercase()) {
                    "e", "entity" -> LivingCleanupService().cleanWorld(worldName, dryRun = dryRun) { result ->
                        RuntimeServices.messages.send(sender, "Cleanup: ${result.cleaned}/${result.total}")
                    }
                    "d", "drop" -> DropCleanupService().cleanWorld(worldName, dryRun = dryRun) { result ->
                        RuntimeServices.messages.send(sender, "Cleanup: ${result.cleaned}/${result.total}")
                    }
                    "c", "chunk" -> ChunkDensityScanner().cleanWorld(worldName, dryRun = dryRun) { result ->
                        RuntimeServices.messages.send(sender, "Cleanup: ${result.cleaned}")
                    }
                    else -> sendUsage(sender)
                }
            }
            else -> sendUsage(sender)
        }
    }

    private fun handleStats(sender: CommandSender, args: Array<out String>) {
        when (args.size) {
            1 -> {
                if (sender !is Player) return
                sender.sendWorldStats(sender.world.name)
            }
            2 -> sender.sendWorldStats(args[1])
            else -> sendUsage(sender)
        }
    }

    private fun handleEntity(sender: CommandSender, args: Array<out String>) {
        when (args.size) {
            2 -> {
                if (sender !is Player) return
                sender.sendEntityStats(sender.world.name, args[1])
            }
            3 -> sender.sendEntityStats(args[2], args[1])
            4 -> {
                val min = args[3].toIntOrNull()
                if (min == null) {
                    RuntimeServices.messages.send(sender, MLang["message.invalid_number", "number" to args[3]])
                    return
                }
                sender.sendEntityStats(args[2], args[1], min)
            }
            else -> sendUsage(sender)
        }
    }

    private fun handleTrash(sender: CommandSender) {
        if (sender !is Player) return
        if (!sender.hasPermission("eclean.trash")) return
        if (!Config.current.trashcan.enabled) {
            RuntimeServices.messages.send(sender, MLang["command.trash_disable"])
            return
        }
        top.e404.eclean.clean.Trashcan.open(sender)
        RuntimeServices.messages.send(sender, MLang["command.trash_open"])
    }

    private fun handlePlayers(sender: CommandSender) {
        sender.sendPlayersStats()
    }

    private fun handleShow(sender: CommandSender) {
        if (sender !is Player) return
        if (!sender.hasPermission("eclean.admin")) return
        val scanner = ChunkDensityScanner()
        scanner.scanDenseEntries { entries ->
            val data = entries
                .map { top.e404.eclean.menu.dense.EntityInfo(it.entityType, it.amount, it.chunk) }
                .toMutableList()
            top.e404.eclean.platform.Schedulers.runGlobal {
                top.e404.eclean.menu.MenuManager.openMenu(top.e404.eclean.menu.dense.DenseMenu(data), sender)
            }
        }
    }
}
