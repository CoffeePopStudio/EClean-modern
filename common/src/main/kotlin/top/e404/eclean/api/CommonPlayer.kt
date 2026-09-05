package top.e404.eclean.common.api

interface CommonCommandSender {
    val name: String
    fun hasPermission(node: String): Boolean
    fun sendMessage(component: net.kyori.adventure.text.Component)
}

interface CommonPlayer : top.e404.eclean.common.api.CommonCommandSender {
    val uniqueId: String
    val worldName: String
    val location: top.e404.eclean.common.api.CommonLocation
}
