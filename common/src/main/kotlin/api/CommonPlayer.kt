package top.e404.eclean.common.api

interface CommonCommandSender {
    val name: String
    fun hasPermission(node: String): Boolean
    fun sendMessage(component: net.kyori.adventure.text.Component)
}

interface CommonPlayer : CommonCommandSender {
    val uniqueId: String
    val worldName: String
    val location: CommonLocation
}
