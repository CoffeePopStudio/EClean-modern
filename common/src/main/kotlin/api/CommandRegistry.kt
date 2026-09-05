package top.e404.eclean.common.api

/** Common command registration contract. Loaders map this to their command API. */
interface CommandRegistry {
    fun register(command: CommonCommand)
    fun unregister(command: CommonCommand)
}

data class CommonCommand(
    val name: String,
    val aliases: List<String> = emptyList(),
    val permission: String? = null,
    val handler: (CommonCommandSender, Array<out String>) -> Boolean,
)
