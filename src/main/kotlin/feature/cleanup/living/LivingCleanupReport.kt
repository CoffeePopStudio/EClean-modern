package top.e404.eclean.feature.cleanup.living

data class LivingCleanupReport(
    val cleaned: Int,
    val total: Int,
    val remainingCandidates: List<LivingCleanupCandidate>,
) {
    fun toResult() = LivingCleanupResult(
        cleaned = cleaned,
        total = total,
    )
}
