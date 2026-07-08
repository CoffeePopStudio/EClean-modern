package top.e404.eclean.platform.runtime

object RuntimePlatformFactory {
    fun create(folia: Boolean): RuntimePlatform = if (folia) FoliaRuntimePlatform else PaperRuntimePlatform
}
