package top.e404.eclean.common.api

/** Common command registration contract. Loaders map this to their command API. */
interface CommandRegistry {
    fun register(command: top.e404.eclean.common.api.CommonCommand)
    fun unregister(command: top.e404.eclean.common.api.CommonCommand)
}

data class CommonCommand(
    val name: String,
    val aliases: List<String> = emptyList(),
    val permission: String? = null,
    val handler: (top.e404.eclean.common.api.CommonCommandSender, Array<out String>) -> Boolean,
)
