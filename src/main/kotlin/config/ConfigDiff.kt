package top.e404.eclean.config

enum class ConfigSection(val displayName: String) {
    GLOBAL("global"),
    CLEANUP("cleanup"),
    DROP("drop"),
    LIVING("living"),
    CHUNK_DENSITY("chunk-density"),
    TRASHCAN("trashcan"),
    PER_WORLD("per-world"),
}

fun ConfigBundle.diff(other: ConfigBundle): Set<ConfigSection> {
    val changed = mutableSetOf<ConfigSection>()
    if (global != other.global) changed += ConfigSection.GLOBAL
    if (cleanup != other.cleanup) changed += ConfigSection.CLEANUP
    if (drop != other.drop) changed += ConfigSection.DROP
    if (living != other.living) changed += ConfigSection.LIVING
    if (chunkDensity != other.chunkDensity) changed += ConfigSection.CHUNK_DENSITY
    if (trashcan != other.trashcan) changed += ConfigSection.TRASHCAN
    if (perWorld != other.perWorld) changed += ConfigSection.PER_WORLD
    return changed
}
