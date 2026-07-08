package top.e404.eclean.feature.cleanup.drop

data class DropCleanupReport(
    val cleaned: Int,
    val total: Int,
) {
    fun toResult() = DropCleanupResult(
        cleaned = cleaned,
        total = total,
    )
}
