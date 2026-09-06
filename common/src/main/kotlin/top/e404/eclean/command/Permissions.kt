package top.e404.eclean.command

object Permissions {
    // Core
    const val DEBUG = "eclean.command.debug"
    const val RELOAD = "eclean.command.reload"
    const val CONFIG = "eclean.command.config"

    // Clean
    const val CLEAN_ALL = "eclean.command.clean.all"
    const val CLEAN_ENTITY = "eclean.command.clean.entity"
    const val CLEAN_DROP = "eclean.command.clean.drop"
    const val CLEAN_CHUNK = "eclean.command.clean.chunk"
    const val CLEAN_TRASH = "eclean.command.clean.trash"
    const val CLEAN_PREVIEW = "eclean.command.clean.preview"

    // Stats
    const val STATS_SELF = "eclean.command.stats.self"
    const val STATS_GUI = "eclean.command.stats.gui"
    const val STATS_WORLD = "eclean.command.stats.world"

    // Entity
    const val ENTITY_SELF = "eclean.command.entity.self"
    const val ENTITY_WORLD = "eclean.command.entity.world"
    const val ENTITY_CHUNK = "eclean.command.entity.chunk"

    // Trash
    const val TRASH_STATS = "eclean.command.trash.stats"
    const val TRASH_OPEN = "eclean.command.trash.open"

    // Other
    const val PLAYERS = "eclean.command.players"
    const val SHOW = "eclean.command.show"
    const val HISTORY = "eclean.command.history"
    const val TELEPORT = "eclean.command.teleport"
    const val TOP_ENTITY = "eclean.command.top.entity"
    const val TOP_CHUNK = "eclean.command.top.chunk"
    const val STATUS_ALL = "eclean.command.status.all"
    const val STATUS_WORLD = "eclean.command.status.world"
    const val ALERTS = "eclean.command.alerts"
}