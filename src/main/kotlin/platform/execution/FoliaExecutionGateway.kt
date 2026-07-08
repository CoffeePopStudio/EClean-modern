package top.e404.eclean.platform.execution

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import top.e404.eclean.platform.SchedulerFacade
import top.e404.eclean.platform.SchedulerHandle

class FoliaExecutionGateway(
    private val scheduler: SchedulerFacade
) : ExecutionGateway {
    override fun runGlobal(task: () -> Unit) = scheduler.runGlobal(task)

    override fun runAsync(task: () -> Unit) = scheduler.runAsync(task)

    override fun runForChunk(chunk: ChunkRef, location: Location, task: () -> Unit) =
        scheduler.runAtLocation(location, task)

    override fun runForEntity(ref: EntityRef, entity: Entity, task: () -> Unit) =
        scheduler.runForEntity(entity, task)

    override fun runForPlayer(ref: PlayerRef, player: Player, task: () -> Unit) =
        scheduler.runForEntity(player, task)

    override fun runLaterForPlayer(
        ref: PlayerRef,
        player: Player,
        delayTicks: Long,
        task: () -> Unit
    ): SchedulerHandle? = scheduler.runLaterForEntity(player, delayTicks, task)
}
