package top.e404.eclean.command

/**
 * Platform-agnostic definition of an EClean root-command subcommand.
 */
data class EcleanCommandSpec(
    val name: String,
    val aliases: List<String> = emptyList(),
    val permission: String? = null,
    val playerOnly: Boolean = false,
)

object EcleanCommandCatalog {
    val entries: List<EcleanCommandSpec> = listOf(
        EcleanCommandSpec("debug", listOf("d"), Permissions.DEBUG),
        EcleanCommandSpec("reload", listOf("r"), Permissions.RELOAD),
        EcleanCommandSpec("config", permission = Permissions.CONFIG),
        EcleanCommandSpec("clean"),
        EcleanCommandSpec("stats", listOf("s")),
        EcleanCommandSpec("status"),
        EcleanCommandSpec("entity", listOf("e")),
        EcleanCommandSpec("trash", listOf("t")),
        EcleanCommandSpec("players", listOf("p"), Permissions.PLAYERS),
        EcleanCommandSpec("show", permission = Permissions.SHOW, playerOnly = true),
        EcleanCommandSpec("history", permission = Permissions.HISTORY),
        EcleanCommandSpec("top"),
        EcleanCommandSpec("tp", permission = Permissions.TELEPORT, playerOnly = true),
    )

    fun find(name: String): EcleanCommandSpec? =
        entries.firstOrNull { it.name == name || it.aliases.any { alias -> alias == name } }
}
