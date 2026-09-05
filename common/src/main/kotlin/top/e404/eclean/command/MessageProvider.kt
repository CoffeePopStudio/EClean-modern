package top.e404.eclean.command

interface MessageProvider {
    fun get(key: String, vararg args: Pair<String, Any>): String
}