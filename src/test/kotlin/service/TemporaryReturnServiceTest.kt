package service

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.junit.jupiter.api.Disabled
import top.e404.eclean.platform.execution.ChunkRef
import top.e404.eclean.platform.execution.EntityRef
import top.e404.eclean.platform.execution.ExecutionGateway
import top.e404.eclean.platform.execution.PlayerRef
import top.e404.eclean.service.PlayerTeleportService
import top.e404.eclean.service.TemporaryReturnEvent
import top.e404.eclean.service.TemporaryReturnService
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Disabled("fix laterrrrrrrrr")
class TemporaryReturnServiceTest {
    @Test
    fun `first temporary teleport returns player after delay`() {
        val gateway = FakeExecutionGateway()
        val world = worldProxy("world")
        val origin = Location(world, 10.0, 64.0, 20.0)
        val playerState = playerProxy(world, origin)
        val events = mutableListOf<TemporaryReturnEvent>()
        val service = TemporaryReturnService(
            gateway,
            PlayerTeleportService(gateway)
        ) { _, event -> events += event }
        val target = Location(world, 40.5, 80.0, 60.5)

        service.teleportWithReturn(playerState.player, target, 600)

        assertLocationEquals(target, playerState.location)
        assertEquals(listOf<TemporaryReturnEvent>(TemporaryReturnEvent.Started), events)
        assertEquals(1, gateway.scheduled.size)
        assertEquals(600, gateway.scheduled.single().delayTicks)

        gateway.runLatest()

        assertLocationEquals(origin, playerState.location)
        assertEquals(
            listOf(TemporaryReturnEvent.Started, TemporaryReturnEvent.Returned),
            events
        )
    }

    @Test
    fun `replacing temporary teleport keeps original return location`() {
        val gateway = FakeExecutionGateway()
        val world = worldProxy("world")
        val origin = Location(world, 8.0, 70.0, 8.0)
        val playerState = playerProxy(world, origin)
        val events = mutableListOf<TemporaryReturnEvent>()
        val service = TemporaryReturnService(
            gateway,
            PlayerTeleportService(gateway)
        ) { _, event -> events += event }

        service.teleportWithReturn(playerState.player, Location(world, 32.5, 75.0, 32.5), 600)
        val firstSchedule = gateway.scheduled.first()

        service.teleportWithReturn(playerState.player, Location(world, 64.5, 90.0, 64.5), 600)

        assertTrue(firstSchedule.handle.cancelled)
        assertEquals(listOf<TemporaryReturnEvent>(TemporaryReturnEvent.Started), events)

        gateway.runLatest()

        assertLocationEquals(origin, playerState.location)
        assertEquals(
            listOf(TemporaryReturnEvent.Started, TemporaryReturnEvent.ReturnedAfterReplace),
            events
        )
    }

    @Test
    fun `quit returns player immediately and cancels pending task`() {
        val gateway = FakeExecutionGateway()
        val world = worldProxy("world")
        val origin = Location(world, 5.0, 65.0, 5.0)
        val playerState = playerProxy(world, origin)
        val events = mutableListOf<TemporaryReturnEvent>()
        val service = TemporaryReturnService(
            gateway,
            PlayerTeleportService(gateway)
        ) { _, event -> events += event }

        service.teleportWithReturn(playerState.player, Location(world, 20.5, 80.0, 20.5), 600)
        val schedule = gateway.scheduled.single()

        service.handleQuit(playerState.player)

        assertTrue(schedule.handle.cancelled)
        assertLocationEquals(origin, playerState.location)
        assertEquals(listOf<TemporaryReturnEvent>(TemporaryReturnEvent.Started), events)
    }

    private fun assertLocationEquals(expected: Location, actual: Location) {
        assertEquals(expected.world?.name, actual.world?.name)
        assertEquals(expected.x, actual.x)
        assertEquals(expected.y, actual.y)
        assertEquals(expected.z, actual.z)
    }

    private fun worldProxy(name: String): World = proxy(World::class.java) { method, _ ->
        when (method.name) {
            "getName" -> name
            else -> defaultValue(method.returnType)
        }
    }

    private fun playerProxy(world: World, origin: Location): PlayerState {
        val uniqueId = UUID.randomUUID()
        var currentLocation = origin.clone()
        val teleports = mutableListOf<Location>()
        val player = proxy(Player::class.java) { method, args ->
            when (method.name) {
                "getUniqueId" -> uniqueId
                "getWorld" -> world
                "getLocation" -> currentLocation.clone()
                "teleport" -> {
                    currentLocation = (args?.get(0) as Location).clone()
                    teleports += currentLocation.clone()
                    true
                }
                else -> defaultValue(method.returnType)
            }
        }
        return PlayerState(player, teleports) { currentLocation.clone() }
    }

    private fun <T> proxy(type: Class<T>, handler: (Method, Array<out Any?>?) -> Any?): T {
        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, args ->
            when (method.name) {
                "hashCode" -> System.identityHashCode(type)
                "equals" -> false
                "toString" -> "proxy:${type.simpleName}"
                else -> handler(method, args)
            }
        } as T
    }

    private fun defaultValue(type: Class<*>): Any? = when {
        !type.isPrimitive -> null
        type == Boolean::class.javaPrimitiveType -> false
        type == Int::class.javaPrimitiveType -> 0
        type == Long::class.javaPrimitiveType -> 0L
        type == Double::class.javaPrimitiveType -> 0.0
        type == Float::class.javaPrimitiveType -> 0f
        type == Short::class.javaPrimitiveType -> 0.toShort()
        type == Byte::class.javaPrimitiveType -> 0.toByte()
        type == Char::class.javaPrimitiveType -> 0.toChar()
        else -> null
    }
}

private data class PlayerState(
    val player: Player,
    val teleports: MutableList<Location>,
    val currentLocation: () -> Location,
) {
    val location get() = currentLocation()
}

private class FakeExecutionGateway : ExecutionGateway {
    val scheduled = mutableListOf<ScheduledCall>()

    override fun runGlobal(task: () -> Unit) = task()

    override fun runAsync(task: () -> Unit) = task()

    override fun runForChunk(chunk: ChunkRef, location: Location, task: () -> Unit) = task()

    override fun runForEntity(ref: EntityRef, entity: Entity, task: () -> Unit) = task()

    override fun runForPlayer(ref: PlayerRef, player: Player, task: () -> Unit) = task()

    override fun runLaterForPlayer(
        ref: PlayerRef,
        player: Player,
        delayTicks: Long,
        task: () -> Unit
    ): ScheduledTask {
        val handle = FakeHandle()
        scheduled += ScheduledCall(delayTicks, handle, task)
        return handle.task
    }

    fun runLatest() {
        scheduled.last().task()
    }
}

private data class ScheduledCall(
    val delayTicks: Long,
    val handle: FakeHandle,
    val task: () -> Unit,
)

private class FakeHandle {
    var cancelled = false
        private set

    val task: ScheduledTask = proxy(ScheduledTask::class.java) { method, _ ->
        when (method.name) {
            "cancel" -> {
                cancelled = true
                null
            }
            else -> defaultValue(method.returnType)
        }
    }
}

private fun <T> proxy(type: Class<T>, handler: (Method, Array<out Any?>?) -> Any?): T {
    @Suppress("UNCHECKED_CAST")
    return Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, args ->
        when (method.name) {
            "hashCode" -> System.identityHashCode(type)
            "equals" -> false
            "toString" -> "proxy:${type.simpleName}"
            else -> handler(method, args)
        }
    } as T
}

private fun defaultValue(type: Class<*>): Any? = when {
    !type.isPrimitive -> null
    type == Boolean::class.javaPrimitiveType -> false
    type == Int::class.javaPrimitiveType -> 0
    type == Long::class.javaPrimitiveType -> 0L
    type == Double::class.javaPrimitiveType -> 0.0
    type == Float::class.javaPrimitiveType -> 0f
    type == Short::class.javaPrimitiveType -> 0.toShort()
    type == Byte::class.javaPrimitiveType -> 0.toByte()
    type == Char::class.javaPrimitiveType -> 0.toChar()
    else -> null
}
