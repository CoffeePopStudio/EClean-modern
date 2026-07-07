package top.e404.eclean.platform

import org.bukkit.Location
import org.bukkit.entity.Entity

sealed interface ExecutionContext {
    data object Global : ExecutionContext
    data class Region(val location: Location) : ExecutionContext
    data class EntityTarget(val entity: Entity) : ExecutionContext
    data object Async : ExecutionContext
}
