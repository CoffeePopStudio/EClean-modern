package platform

import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import top.e404.eclean.platform.SchedulerFacade
import top.e404.eclean.platform.SchedulerHandle
import top.e404.eclean.platform.execution.ChunkRef
import top.e404.eclean.platform.execution.PaperExecutionGateway
import top.e404.eclean.platform.execution.PlayerRef

class PaperExecutionGatewayTest {
    @Test
    fun `paper gateway routes player and chunk execution to scheduler`() {
        val calls = mutableListOf<String>()
        val scheduler = object : SchedulerFacade {
            override fun runGlobal(task: () -> Unit) {
                calls += "global"
                task()
            }

            override fun runAsync(task: () -> Unit) {
                calls += "async"
                task()
            }

            override fun runAtLocation(location: Location, task: () -> Unit) {
                calls += "location:${location.blockX},${location.blockZ}"
                task()
            }

            override fun runForEntity(entity: Entity, task: () -> Unit) {
                calls += "entity:${entity.uniqueId}"
                task()
            }

            override fun runLaterGlobal(delayTicks: Long, task: () -> Unit): SchedulerHandle? = null

            override fun runLaterForEntity(entity: Entity, delayTicks: Long, task: () -> Unit): SchedulerHandle? = null

            override fun scheduleRepeatingGlobal(delayTicks: Long, periodTicks: Long, task: () -> Unit): SchedulerHandle? = null

            override fun cancelPluginTasks() = Unit
        }
        val world = worldProxy("world")
        val playerId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val player = playerProxy(playerId)
        val gateway = PaperExecutionGateway(scheduler)

        gateway.runForPlayer(PlayerRef(playerId, world.name, 10.0, 64.0, 20.0), player) {}
        gateway.runForChunk(ChunkRef(world.name, 1, 2), Location(world, 24.0, 64.0, 40.0)) {}

        assertEquals(
            listOf(
                "entity:00000000-0000-0000-0000-000000000001",
                "location:24,40",
            ),
            calls
        )
    }

    private fun worldProxy(name: String): World = proxy(World::class.java) { method, _ ->
        when (method.name) {
            "getName" -> name
            else -> defaultValue(method.returnType)
        }
    }

    private fun playerProxy(uniqueId: UUID): Player = proxy(Player::class.java) { method, _ ->
        when (method.name) {
            "getUniqueId" -> uniqueId
            else -> defaultValue(method.returnType)
        }
    }

    private fun <T> proxy(type: Class<T>, handler: (java.lang.reflect.Method, Array<out Any?>?) -> Any?): T {
        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, args ->
            when (method.name) {
                "hashCode" -> System.identityHashCode(this)
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
