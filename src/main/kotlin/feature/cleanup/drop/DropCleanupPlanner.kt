package top.e404.eclean.feature.cleanup.drop

import org.bukkit.Bukkit
import org.bukkit.World
import top.e404.eclean.config.Config

class DropCleanupPlanner {
    fun planWorlds(): List<World> {
        val disabledWorlds = Config.current.drop.disabledWorlds
        return Bukkit.getWorlds().filterNot { world ->
            disabledWorlds.any { regex -> world.name matches regex }
        }
    }
}
