package top.e404.eclean.config

import top.e404.eclean.config.model.ChunkDensityConfig
import top.e404.eclean.config.model.CleanupConfig
import top.e404.eclean.config.model.DropConfig
import top.e404.eclean.config.model.GlobalConfig
import top.e404.eclean.config.model.LivingConfig
import top.e404.eclean.config.model.PerWorldConfig
import top.e404.eclean.config.model.TrashcanConfig

data class ConfigBundle(
    val global: GlobalConfig = GlobalConfig(),
    val cleanup: CleanupConfig = CleanupConfig(),
    val drop: DropConfig = DropConfig(),
    val living: LivingConfig = LivingConfig(),
    val chunkDensity: ChunkDensityConfig = ChunkDensityConfig(),
    val trashcan: TrashcanConfig = TrashcanConfig(),
    val perWorld: PerWorldConfig = PerWorldConfig(),
)
