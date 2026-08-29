package top.e404.eclean.command
import top.e404.eclean.PL

import org.bukkit.command.CommandSender
import top.e404.eclean.clean.Trashcan.cleanTrash
import top.e404.eclean.clean.cleanDenseEntities
import top.e404.eclean.clean.cleanDrop
import top.e404.eclean.clean.cleanLiving
import top.e404.eclean.feature.cleanup.chunk.ChunkDensityScanner
import top.e404.eclean.feature.cleanup.drop.DropCleanupService
import top.e404.eclean.feature.cleanup.living.LivingCleanupService
import top.e404.eclean.lang.MLang

object CleanCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        val dryRun = args.contains("--preview")
        val filteredArgs = args.filter { it != "--preview" }.toTypedArray()

        when (filteredArgs.size) {
            1 -> PL.services.cleanupCoordinator.cleanNow(dryRun = dryRun) {
                if (dryRun) PL.services.messages.send(sender, MLang["command.clean_dry_done"])
            }
            2 -> when (filteredArgs[1].lowercase()) {
                "e", "entity" -> {
                    if (dryRun) {
                        LivingCleanupService().cleanAllWorlds(dryRun = true) { results ->
                            val total = results.sumOf { it.total }
                            val cleaned = results.sumOf { it.cleaned }
                            PL.services.messages.send(
                                sender,
                                MLang["command.clean_dry_living", "cleaned" to cleaned, "total" to total, "worlds" to results.size],
                            )
                        }
                    } else { cleanLiving() }
                }
                "d", "drop" -> {
                    if (dryRun) {
                        DropCleanupService().cleanAllWorlds(dryRun = true) { results ->
                            val total = results.sumOf { it.total }
                            val cleaned = results.sumOf { it.cleaned }
                            PL.services.messages.send(
                                sender,
                                MLang["command.clean_dry_drop", "cleaned" to cleaned, "total" to total, "worlds" to results.size],
                            )
                        }
                    } else { cleanDrop() }
                }
                "c", "chunk" -> {
                    if (dryRun) {
                        ChunkDensityScanner().cleanAllWorlds(dryRun = true) { result ->
                            PL.services.messages.send(
                                sender,
                                MLang["command.clean_dry_chunk", "cleaned" to result.cleaned, "regions" to result.denseEntries.size],
                            )
                        }
                    } else { cleanDenseEntities() }
                }
                "t", "trash" -> cleanTrash()
                else -> Commands.sendUsage(sender)
            }
            3 -> {
                val worldName = filteredArgs[2]
                when (filteredArgs[1].lowercase()) {
                    "e", "entity" -> LivingCleanupService().cleanWorld(worldName, dryRun = dryRun) { result ->
                        PL.services.messages.send(
                            sender,
                            MLang["command.clean_world_result", "cleaned" to result.cleaned, "total" to result.total],
                        )
                    }
                    "d", "drop" -> DropCleanupService().cleanWorld(worldName, dryRun = dryRun) { result ->
                        PL.services.messages.send(
                            sender,
                            MLang["command.clean_world_result", "cleaned" to result.cleaned, "total" to result.total],
                        )
                    }
                    "c", "chunk" -> ChunkDensityScanner().cleanWorld(worldName, dryRun = dryRun) { result ->
                        PL.services.messages.send(sender, MLang["command.clean_world_chunk_result", "cleaned" to result.cleaned])
                    }
                    else -> Commands.sendUsage(sender)
                }
            }
            else -> Commands.sendUsage(sender)
        }
    }
}
