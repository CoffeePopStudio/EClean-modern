package top.e404.eclean.feature.cleanup.drop

import org.bukkit.Bukkit
import top.e404.eclean.config.Config

class DropCleanupPlanner {
    fun planWorldNames(): List<String> {
        val disabledWorlds = Config.current.drop.disabledWorlds
        return Bukkit.getWorlds()
            .filterNot { Config.current.perWorld.worlds[it.name]?.enabled == false }
            .filterNot { world -> disabledWorlds.any { regex -> world.name matches regex } }
            .map { it.name }
    }
}
