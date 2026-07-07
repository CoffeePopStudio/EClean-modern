package top.e404.eclean.platform

object FoliaDetector {
    fun isFolia(): Boolean = try {
        Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}
