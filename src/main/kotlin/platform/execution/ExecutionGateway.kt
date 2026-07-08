package top.e404.eclean.platform.execution

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import top.e404.eclean.platform.SchedulerHandle

interface ExecutionGateway {
    fun runGlobal(task: () -> Unit)
    fun runAsync(task: () -> Unit)
    fun runForChunk(chunk: ChunkRef, location: Location, task: () -> Unit)
    fun runForEntity(ref: EntityRef, entity: Entity, task: () -> Unit)
    fun runForPlayer(ref: PlayerRef, player: Player, task: () -> Unit)
    fun runLaterForPlayer(ref: PlayerRef, player: Player, delayTicks: Long, task: () -> Unit): SchedulerHandle?
}
