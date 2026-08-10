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
import top.e404.eclean.feature.stats.WorldStatsService
import top.e404.eclean.lang.MLang
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.util.formatAsConst
import top.e404.eclean.util.parseSecondAsDuration
import java.util.concurrent.atomic.AtomicInteger

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
            "t", "trash" -> handleTrash(sender, args)
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
                if (args.size == 2) return EntityType.values().map { it.name }.filter { it.startsWith(args[1].uppercase()) }
                if (args.size == 3) return Bukkit.getWorlds().map { it.name }.filter { it.startsWith(args[2].lowercase()) }
            }
            "t", "trash" -> {
                if (args.size == 2) return listOf("stats").filter { it.startsWith(args[1].lowercase()) }
            }
        }
        return emptyList()
    }

    private fun sendUsage(sender: CommandSender) {
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
        for (key in keys) RuntimeServices.messages.send(sender, MLang[key])
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
                if (dryRun) RuntimeServices.messages.send(sender, "<green>Preview complete — no entities were removed</green>")
            }
            2 -> when (filteredArgs[1].lowercase()) {
                "e", "entity" -> {
                    if (dryRun) {
                        LivingCleanupService().cleanAllWorlds(dryRun = true) { results ->
                            val total = results.sumOf { it.total }
                            val cleaned = results.sumOf { it.cleaned }
                            RuntimeServices.messages.send(sender, "<green>Preview — Living: <gold>$cleaned</gold>/<gold>$total</gold> entities across <gold>${results.size}</gold> worlds</green>")
                        }
                    } else { cleanLiving() }
                }
                "d", "drop" -> {
                    if (dryRun) {
                        DropCleanupService().cleanAllWorlds(dryRun = true) { results ->
                            val total = results.sumOf { it.total }
                            val cleaned = results.sumOf { it.cleaned }
                            RuntimeServices.messages.send(sender, "<green>Preview — Drop: <gold>$cleaned</gold>/<gold>$total</gold> items across <gold>${results.size}</gold> worlds</green>")
                        }
                    } else { cleanDrop() }
                }
                "c", "chunk" -> {
                    if (dryRun) {
                        ChunkDensityScanner().cleanAllWorlds(dryRun = true) { result ->
                            RuntimeServices.messages.send(sender, "<green>Preview — Chunk density: <gold>${result.cleaned}</gold> entities across <gold>${result.denseEntries.size}</gold> dense regions</green>")
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

    private fun handleTrash(sender: CommandSender, args: Array<out String>) {
        if (args.size == 2 && args[1].equals("stats", true)) {
            if (!sender.hasPermission("eclean.admin")) return
            sender.sendTrashStats()
            return
        }
        if (sender !is Player) return
        if (!sender.hasPermission("eclean.trash")) return
        if (!Config.current.trashcan.enabled) {
            RuntimeServices.messages.send(sender, MLang["command.trash_disable"])
            return
        }
        top.e404.eclean.clean.Trashcan.open(sender)
        RuntimeServices.messages.send(sender, MLang["command.trash_open"])
    }

    private fun CommandSender.sendTrashStats() {
        val entries = RuntimeServices.trashcanManager.stats()
        if (entries.isEmpty()) {
            RuntimeServices.messages.send(this, MLang["command.trash_stats_empty"])
            return
        }
        RuntimeServices.messages.send(this, MLang["command.trash_stats_header", "count" to entries.size])
        val now = System.currentTimeMillis()
        for (entry in entries) {
            val expire = entry.deadline.takeIf { it != Long.MAX_VALUE }
                ?.let { maxOf(0, (it - now) / 1000) }
                ?.parseSecondAsDuration()
                ?: MLang["command.trash_never_expire"]
            RuntimeServices.messages.send(
                this,
                MLang["command.trash_stats_line", "item" to entry.prototype.type.name, "amount" to entry.count, "expire" to expire],
            )
        }
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
                top.e404.eclean.menu.dense.DenseMenu(data).open(sender)
            }
        }
    }

    private fun CommandSender.sendWorldStats(worldName: String) {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            RuntimeServices.messages.send(this, "<red>不存在名为<yellow>$worldName</yellow>的世界</red>")
            return
        }
        val service = WorldStatsService()
        service.collectWorldStats(worldName) { result ->
            if (result == null) {
                RuntimeServices.messages.send(this, "<red>收集世界统计信息失败</red>")
                return@collectWorldStats
            }
            if (result.totalEntities == 0) {
                RuntimeServices.messages.send(this, MLang["command.stats.empty"])
                return@collectWorldStats
            }
            val entity = result.sortedEntries().joinToString(MLang["command.stats.spacing"]) { (k, v) ->
                MLang[
                    "command.stats.content",
                    "type" to k,
                    "count" to v.withColor()
                ]
            }
            RuntimeServices.messages.send(
                this,
                MLang[
                    "command.stats.world",
                    "world" to worldName,
                    "count" to result.loadedChunks,
                    "force" to result.forceLoadedChunks,
                    "entity" to entity
                ]
            )
        }
    }

    private fun CommandSender.sendEntityStats(worldName: String, typeName: String, min: Int = 0) {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            RuntimeServices.messages.send(this, "<red>不存在名为<yellow>$worldName</yellow>的世界</red>")
            return
        }
        val type = try {
            EntityType.valueOf(typeName.formatAsConst())
        } catch (t: Throwable) {
            RuntimeServices.messages.send(this, MLang["message.invalid_entity_type"])
            return
        }
        val service = WorldStatsService()
        service.collectEntityStats(worldName, type, min) { entries ->
            if (entries.isEmpty()) {
                RuntimeServices.messages.send(this, MLang["command.stats.empty"])
                return@collectEntityStats
            }
            val entity = entries.joinToString(MLang["command.stats.spacing"]) { (label, v) ->
                MLang[
                    "command.stats.content",
                    "type" to label,
                    "count" to v.withColor()
                ]
            }
            RuntimeServices.messages.send(
                this,
                MLang[
                    "command.stats.entity",
                    "type" to typeName,
                    "entity" to entity
                ]
            )
        }
    }

    private fun CommandSender.sendPlayersStats() {
        Schedulers.runGlobal {
            val players = Bukkit.getOnlinePlayers().toList()
            if (players.isEmpty()) {
                RuntimeServices.messages.send(this, MLang["command.stats.empty"])
                return@runGlobal
            }
            val byWorld = players.groupBy { it.world.name to it.world }
            val pending = AtomicInteger(players.size)
            val lines = LinkedHashMap<String, MutableList<String>>()
            byWorld.values.forEach { list ->
                val worldName = list.first().world.name
                val worldLines = mutableListOf<String>()
                synchronized(lines) { lines[worldName] = worldLines }
                list.forEach { player ->
                    Schedulers.runForEntity(player) {
                        val loc = player.location
                        val entry = "  <aqua>${player.name}</aqua><white>: ${loc.blockX} ${loc.blockY} ${loc.blockZ}</white>"
                        synchronized(worldLines) { worldLines += entry }
                        if (pending.decrementAndGet() == 0) sendPlayerResult(this@sendPlayersStats, lines)
                    }
                }
            }
        }
    }

    private fun sendPlayerResult(sender: CommandSender, lines: Map<String, List<String>>) {
        Schedulers.runGlobal {
            lines.forEach { (worldName, worldLines) ->
                RuntimeServices.messages.send(sender, "<gold>$worldName</gold>:${worldLines.joinToString("")}")
            }
        }
    }

    private fun Int.withColor() = when {
        this > 60 -> "<red>$this</red>"
        this > 30 -> "<yellow>$this</yellow>"
        else -> "<green>$this</green>"
    }
}
