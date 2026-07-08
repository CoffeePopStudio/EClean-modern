package top.e404.eclean.util

val String.color get() = replace("&", "§")

private val colorRegex = Regex("(?i)[§&][\\da-fk-orx]")

fun String.removeColor() = replace(colorRegex, "")

private val constRegex = Regex("[.\\s\\-_]+")

fun String.formatAsConst() = replace(constRegex, "_").uppercase()

fun String.placeholder(vararg placeholder: Pair<String, Any?>): String {
    var s = this
    for ((k, v) in placeholder) s = s.replace("{$k}", v.toString())
    return s.color
}

fun String.placeholder(placeholder: Map<String, Any?>): String {
    var s = this
    for ((k, v) in placeholder.entries) s = s.replace("{$k}", v.toString())
    return s.color
}
