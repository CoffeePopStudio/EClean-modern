package top.e404.eclean.feature.cleanup.living

import org.bukkit.Bukkit
import org.bukkit.World
import top.e404.eclean.config.Config

class LivingCleanupPlanner {
    fun planWorlds(): List<World> {
        val disabledWorlds = Config.current.living.disabledWorlds
        return Bukkit.getWorlds().filterNot { world ->
            disabledWorlds.any { regex -> world.name matches regex }
        }
    }
}
