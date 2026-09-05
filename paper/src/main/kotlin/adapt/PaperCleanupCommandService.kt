package top.e404.eclean.paper.adapt

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import top.e404.eclean.PL
import top.e404.eclean.clean.cleanDenseEntities
import top.e404.eclean.clean.cleanDrop
import top.e404.eclean.clean.cleanLiving
import top.e404.eclean.common.api.CommonCommandSender
import top.e404.eclean.feature.cleanup.CleanupCommandService
import top.e404.eclean.feature.cleanup.chunk.ChunkDensityScanner
import top.e404.eclean.feature.cleanup.drop.DropCleanupService
import top.e404.eclean.feature.cleanup.living.LivingCleanupService
import top.e404.eclean.lang.MLang

fun CommonCommandSender.toBukkitSender(): CommandSender =
    runCatching { Bukkit.getPlayer(name) }.getOrNull() ?: Bukkit.getConsoleSender()

class PaperCleanupCommandService : CleanupCommandService {
    override fun cleanAll(sender: CommonCommandSender, dryRun: Boolean) {
        val bukkit = sender.toBukkitSender()
        PL.services.cleanupCoordinator.cleanNow(dryRun = dryRun) {
            if (dryRun) PL.services.messages.send(bukkit, MLang["command.clean_dry_done"])
        }
    }

    override fun cleanEntity(sender: CommonCommandSender, world: String?, dryRun: Boolean) {
        val bukkit = sender.toBukkitSender()
        if (world == null) {
            if (dryRun) {
                LivingCleanupService().cleanAllWorlds(dryRun = true) { results ->
                    PL.services.messages.send(
                        bukkit,
                        MLang[
                            "command.clean_dry_living",
                            "cleaned" to results.sumOf { it.cleaned },
                            "total" to results.sumOf { it.total },
                            "worlds" to results.size,
                        ],
                    )
                }
            } else {
                cleanLiving()
            }
        } else {
            LivingCleanupService().cleanWorld(world, dryRun = dryRun) { result ->
                PL.services.messages.send(
                    bukkit,
                    MLang["command.clean_world_result", "cleaned" to result.cleaned, "total" to result.total],
                )
            }
        }
    }

    override fun cleanDrop(sender: CommonCommandSender, world: String?, dryRun: Boolean) {
        val bukkit = sender.toBukkitSender()
        if (world == null) {
            if (dryRun) {
                DropCleanupService().cleanAllWorlds(dryRun = true) { results ->
                    PL.services.messages.send(
                        bukkit,
                        MLang[
                            "command.clean_dry_drop",
                            "cleaned" to results.sumOf { it.cleaned },
                            "total" to results.sumOf { it.total },
                            "worlds" to results.size,
                        ],
                    )
                }
            } else {
                cleanDrop()
            }
        } else {
            DropCleanupService().cleanWorld(world, dryRun = dryRun) { result ->
                PL.services.messages.send(
                    bukkit,
                    MLang["command.clean_world_result", "cleaned" to result.cleaned, "total" to result.total],
                )
            }
        }
    }

    override fun cleanChunk(sender: CommonCommandSender, world: String?, dryRun: Boolean) {
        val bukkit = sender.toBukkitSender()
        if (world == null) {
            if (dryRun) {
                ChunkDensityScanner().cleanAllWorlds(dryRun = true) { result ->
                    PL.services.messages.send(
                        bukkit,
                        MLang[
                            "command.clean_dry_chunk",
                            "cleaned" to result.cleaned,
                            "regions" to result.denseEntries.size,
                        ],
                    )
                }
            } else {
                cleanDenseEntities()
            }
        } else {
            ChunkDensityScanner().cleanWorld(world, dryRun = dryRun) { result ->
                PL.services.messages.send(
                    bukkit,
                    MLang["command.clean_world_chunk_result", "cleaned" to result.cleaned],
                )
            }
        }
    }

    override fun cleanTrash(sender: CommonCommandSender, dryRun: Boolean) {
        val bukkit = sender.toBukkitSender()
        if (dryRun) {
            PL.services.messages.send(bukkit, MLang["command.clean_dry_done"])
        } else {
            top.e404.eclean.clean.Trashcan.cleanTrash()
        }
    }
}
