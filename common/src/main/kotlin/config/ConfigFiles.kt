package top.e404.eclean.config

enum class ConfigFiles(
    val diskName: String,
    val resourcePath: String,
) {
    GLOBAL("config.yml", "config/config.yml"),
    CLEANUP("cleanup.yml", "config/cleanup.yml"),
    DROP("drop.yml", "config/drop.yml"),
    LIVING("living.yml", "config/living.yml"),
    CHUNK_DENSITY("chunk-density.yml", "config/chunk-density.yml"),
    TRASHCAN("trashcan.yml", "config/trashcan.yml"),
    PER_WORLD("per-world.yml", "config/per-world.yml");
}
