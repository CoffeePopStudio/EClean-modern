package top.e404.eclean.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import top.e404.eclean.PL
import top.e404.eclean.feature.stats.WorldStatsResult
import top.e404.eclean.feature.stats.WorldStatsService
import top.e404.eclean.lang.MLang

object TopCommand {
    fun handle(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("eclean.admin")) return
        val sub = args.getOrNull(1)?.lowercase()
        val rest = args.drop(2)
        val limit = rest.firstNotNullOfOrNull { it.toIntOrNull() } ?: 10
        val world = rest.firstOrNull { it.toIntOrNull() == null }

        val service = WorldStatsService()
        when (sub) {
            "entity" -> sendEntityTop(sender, service, world, limit)
            "chunk" -> sendChunkTop(sender, service, world, limit)
            else -> Commands.sendUsage(sender)
        }
    }

    private fun sendEntityTop(
        sender: CommandSender,
        service: WorldStatsService,
        world: String?,
        limit: Int,
    ) {
        val onResult: (List<Pair<String, WorldStatsResult>>) -> Unit = { results ->
            val merged = mutableMapOf<EntityType, Int>()
            results.forEach { (_, result) ->
                result.entityCounts.forEach { (type, count) ->
                    merged[type] = (merged[type] ?: 0) + count
                }
            }
            sendTop(sender, "entity", merged.entries.sortedByDescending { it.value }.take(limit)) { (index, entry) ->
                MLang["command.top_entity", "rank" to index + 1, "type" to entry.key.name, "count" to entry.value]
            }
        }
        if (world != null) {
            service.collectWorldStats(world) { result ->
                onResult(if (result == null) emptyList() else listOf(world to result))
            }
        } else {
            service.collectAllWorldStats(onResult)
        }
    }

    private fun sendChunkTop(
        sender: CommandSender,
        service: WorldStatsService,
        world: String?,
        limit: Int,
    ) {
        service.collectChunkTotals(world) { totals ->
            sendTop(sender, "chunk", totals.take(limit)) { (index, total) ->
                MLang[
                    "command.top_chunk",
                    "rank" to index + 1,
                    "world" to total.worldName,
                    "x" to total.chunkX * 16,
                    "z" to total.chunkZ * 16,
                    "count" to total.count,
                ]
            }
        }
    }

    private fun <T> sendTop(
        sender: CommandSender,
        type: String,
        entries: List<T>,
        line: (IndexedValue<T>) -> String,
    ) {
        if (entries.isEmpty()) {
            PL.services.messages.send(sender, MLang["command.stats.empty"])
            return
        }
        PL.services.messages.send(sender, MLang["command.top_header", "type" to type])
        entries.forEachIndexed { index, entry ->
            PL.services.messages.send(sender, line(IndexedValue(index, entry)))
        }
    }
}
