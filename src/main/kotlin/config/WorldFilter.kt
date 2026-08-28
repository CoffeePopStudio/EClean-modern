package top.e404.eclean.config

import org.bukkit.Bukkit

fun planEnabledWorlds(disabledWorlds: List<Regex>): List<String> =
    Bukkit.getWorlds()
        .filterNot { Config.current.perWorld.worlds[it.name]?.enabled == false }
        .filterNot { world -> disabledWorlds.any { regex -> world.name matches regex } }
        .map { it.name }
