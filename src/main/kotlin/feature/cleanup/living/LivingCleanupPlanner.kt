package top.e404.eclean.feature.cleanup.living

import org.bukkit.Bukkit
import top.e404.eclean.config.Config

class LivingCleanupPlanner {
    fun planWorldNames(): List<String> {
        val disabledWorlds = Config.current.living.disabledWorlds
        return Bukkit.getWorlds()
            .filterNot { world -> disabledWorlds.any { regex -> world.name matches regex } }
            .map { it.name }
    }
}
