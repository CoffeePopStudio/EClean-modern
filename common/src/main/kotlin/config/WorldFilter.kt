package top.e404.eclean.config

import top.e404.eclean.config.model.PerWorldEntry

fun planEnabledWorlds(
    worldNames: List<String>,
    disabledWorlds: List<Regex>,
    perWorld: Map<String, PerWorldEntry>,
): List<String> =
    worldNames
        .filterNot { perWorld[it]?.enabled == false }
        .filterNot { world -> disabledWorlds.any { regex -> world.matches(regex) } }
