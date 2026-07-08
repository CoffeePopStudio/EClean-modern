package top.e404.eclean.platform.execution

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

interface ExecutionGateway {
    fun runGlobal(task: () -> Unit)
    fun runAsync(task: () -> Unit)
    fun runForChunk(chunk: ChunkRef, location: Location, task: () -> Unit)
    fun runForEntity(ref: EntityRef, entity: Entity, task: () -> Unit)
    fun runForPlayer(ref: PlayerRef, player: Player, task: () -> Unit)
    fun runLaterForPlayer(ref: PlayerRef, player: Player, delayTicks: Long, task: () -> Unit): ScheduledTask?
}
