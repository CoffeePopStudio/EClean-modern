package top.e404.eclean.feature.cleanup

data class CleanupRecord(
    val timestamp: Long,
    val worldName: String?,
    val drop: Int,
    val living: Int,
    val chunk: Int,
)

class CleanupHistoryService(
    private val maxSize: Int = 100,
) {
    private val records = mutableListOf<CleanupRecord>()

    @Synchronized
    fun record(worldName: String?, drop: Int, living: Int, chunk: Int) {
        records.add(CleanupRecord(System.currentTimeMillis(), worldName, drop, living, chunk))
        if (records.size > maxSize) {
            records.removeAt(0)
        }
    }

    @Synchronized
    fun recent(limit: Int = 10): List<CleanupRecord> =
        records.takeLast(limit.coerceAtLeast(1)).reversed()

    @Synchronized
    fun count(): Int = records.size
}
