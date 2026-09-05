package top.e404.eclean.config

fun Iterable<Regex>.matches(value: String): Boolean = any { value.matches(it) }
