package top.e404.eclean.util

private val constRegex = Regex("[.\\s\\-_]+")

fun String.formatAsConst() = replace(constRegex, "_").uppercase()

fun String.placeholder(vararg placeholder: Pair<String, Any?>): String =
    placeholder(mapOf(*placeholder))

fun String.placeholder(placeholder: Map<String, Any?>): String {
    var s = this
    for ((k, v) in placeholder.entries) s = s.replace("{$k}", v.toString())
    return s
}

fun Long.parseSecondAsDuration(): String {
    if (this <= 0) return "0s"
    val h = this / 3600
    val m = this % 3600 / 60
    val s = this % 60
    return buildString {
        if (h > 0) append("${h}h ")
        if (m > 0) append("${m}min ")
        append("${s}s")
    }.trim()
}
