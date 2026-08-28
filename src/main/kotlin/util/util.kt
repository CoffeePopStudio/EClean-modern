package top.e404.eclean.util

import org.bukkit.entity.Entity


fun Collection<Entity>.info(): Map<String, Int> {
    val map = mutableMapOf<String, Int>()
    for (entity in this) map.compute(entity.type.name) { _, v -> (v ?: 0) + 1 }
    return map
}

fun String.isMatch(list: List<Regex>) = list.firstOrNull { it matches this }

fun <T> Map<String, List<T>>.filterByMatchers(
    matchers: List<Regex>,
    blackList: Boolean,
): Map<String, List<T>> =
    if (blackList) filterKeys { type -> type.isMatch(matchers) != null }
    else filterKeys { type -> type.isMatch(matchers) == null }