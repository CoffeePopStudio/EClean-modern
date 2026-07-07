package top.e404.eclean.feature.cleanup.living

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import top.e404.eclean.PL
import top.e404.eclean.config.Config
import top.e404.eclean.util.info
import top.e404.eclean.util.isMatch

class LivingCleanupService {
    fun cleanAllWorlds(): List<LivingCleanupResult> {
        val cfg = Config.current.living
        val worlds = Bukkit.getWorlds().filterNot { world ->
            cfg.disabledWorlds.any { regex -> world.name matches regex }
        }
        return worlds.map(::cleanWorld)
    }

    fun cleanWorld(world: World): LivingCleanupResult {
        val cfg = Config.current.living
        val all = world.livingEntities.filterNot { it is Player }.toMutableList()
        val total = all.size

        if (!cfg.settings.cleanNamed) all.removeIf { it.customName != null }
        if (!cfg.settings.cleanLeashed) all.removeIf { it.isLeashed }
        if (!cfg.settings.cleanMounted) all.removeIf { it.isInsideVehicle || it.passengers.isNotEmpty() }

        val groupBy = mutableMapOf<String, MutableList<LivingEntity>>()
        for (entity in all) {
            groupBy.getOrPut(entity.type.name) { mutableListOf() }.add(entity)
        }

        if (cfg.blacklistMode) {
            groupBy.entries.removeIf { (type, _) -> type.isMatch(cfg.matchers) == null }
        } else {
            groupBy.entries.removeIf { (type, _) -> type.isMatch(cfg.matchers) != null }
        }

        var cleaned = 0
        groupBy.values.forEach { list ->
            cleaned += list.size
            list.forEach(LivingEntity::remove)
        }
        PL.debug { "世界${world.name}生物清理完成($cleaned/${total})" }
        PL.buildDebug {
            append("世界").append(world.name).append("生物统计: ")
            all.info().entries.joinTo(this, ", ") { (type, count) -> "$type: $count" }
        }
        return LivingCleanupResult(cleaned = cleaned, total = total)
    }
}
