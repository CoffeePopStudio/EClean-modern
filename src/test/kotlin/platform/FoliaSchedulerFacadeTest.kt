package platform

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler
import io.papermc.paper.threadedregions.scheduler.EntityScheduler
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import io.papermc.paper.threadedregions.scheduler.RegionScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import top.e404.eclean.platform.FoliaSchedulerFacade
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FoliaSchedulerFacadeTest {
    @Test
    fun `folia facade routes immediate execution through Folia schedulers`() {
        val calls = mutableListOf<String>()
        val scheduledTask = scheduledTaskProxy { calls += "scheduled.cancel" }
        val globalScheduler = globalSchedulerProxy(calls, scheduledTask)
        val asyncScheduler = asyncSchedulerProxy(calls, scheduledTask)
        val regionScheduler = regionSchedulerProxy(calls, scheduledTask)
        val entityScheduler = entitySchedulerProxy(calls, scheduledTask)
        val server = serverProxy(globalScheduler, asyncScheduler, regionScheduler)
        val plugin = pluginProxy(server)
        val world = worldProxy("world")
        val entity = entityProxy(entityScheduler)
        val facade = FoliaSchedulerFacade(plugin)

        facade.runGlobal { calls += "task:global" }
        facade.runAsync { calls += "task:async" }
        facade.runAtLocation(Location(world, 24.0, 64.0, 40.0)) { calls += "task:location" }
        facade.runForEntity(entity) { calls += "task:entity" }

        assertEquals(
            listOf(
                "global.execute",
                "task:global",
                "async.runNow",
                "task:async",
                "region.execute",
                "task:location",
                "entity.run",
                "task:entity",
            ),
            calls
        )
    }

    @Test
    fun `folia facade wraps delayed tasks and plugin cancellation`() {
        val calls = mutableListOf<String>()
        val globalDelayed = scheduledTaskProxy { calls += "cancel:globalDelayed" }
        val entityDelayed = scheduledTaskProxy { calls += "cancel:entityDelayed" }
        val repeating = scheduledTaskProxy { calls += "cancel:repeating" }
        val globalScheduler = globalSchedulerProxy(calls, globalDelayed, repeating)
        val asyncScheduler = asyncSchedulerProxy(calls, globalDelayed)
        val regionScheduler = regionSchedulerProxy(calls, globalDelayed)
        val entityScheduler = entitySchedulerProxy(calls, entityDelayed)
        val server = serverProxy(globalScheduler, asyncScheduler, regionScheduler)
        val plugin = pluginProxy(server)
        val entity = entityProxy(entityScheduler)
        val facade = FoliaSchedulerFacade(plugin)

        val delayedHandle = facade.runLaterGlobal(20) { calls += "task:globalDelayed" }
        val entityHandle = facade.runLaterForEntity(entity, 40) { calls += "task:entityDelayed" }
        val repeatingHandle = facade.scheduleRepeatingGlobal(20, 20) { calls += "task:repeating" }

        assertNotNull(delayedHandle)
        assertNotNull(entityHandle)
        assertNotNull(repeatingHandle)

        delayedHandle.cancel()
        entityHandle.cancel()
        repeatingHandle.cancel()
        facade.cancelPluginTasks()

        assertEquals(
            listOf(
                "global.runDelayed:20",
                "task:globalDelayed",
                "entity.runDelayed:40",
                "task:entityDelayed",
                "global.runAtFixedRate:20:20",
                "task:repeating",
                "cancel:globalDelayed",
                "cancel:entityDelayed",
                "cancel:repeating",
                "global.cancelTasks",
                "async.cancelTasks",
            ),
            calls
        )
    }

    private fun serverProxy(
        globalScheduler: GlobalRegionScheduler,
        asyncScheduler: AsyncScheduler,
        regionScheduler: RegionScheduler,
    ): Server = proxy(Server::class.java) { method, _ ->
        when (method.name) {
            "getGlobalRegionScheduler" -> globalScheduler
            "getAsyncScheduler" -> asyncScheduler
            "getRegionScheduler" -> regionScheduler
            else -> defaultValue(method.returnType)
        }
    }

    private fun pluginProxy(server: Server): Plugin = proxy(Plugin::class.java) { method, _ ->
        when (method.name) {
            "getServer" -> server
            else -> defaultValue(method.returnType)
        }
    }

    private fun worldProxy(name: String): World = proxy(World::class.java) { method, _ ->
        when (method.name) {
            "getName" -> name
            else -> defaultValue(method.returnType)
        }
    }

    private fun entityProxy(entityScheduler: EntityScheduler): Entity = proxy(Entity::class.java) { method, _ ->
        when (method.name) {
            "getScheduler" -> entityScheduler
            else -> defaultValue(method.returnType)
        }
    }

    private fun globalSchedulerProxy(
        calls: MutableList<String>,
        delayedTask: ScheduledTask,
        repeatingTask: ScheduledTask = delayedTask,
    ): GlobalRegionScheduler = proxy(GlobalRegionScheduler::class.java) { method, args ->
        when (method.name) {
            "execute" -> {
                calls += "global.execute"
                (args?.get(1) as Runnable).run()
                null
            }

            "runDelayed" -> {
                calls += "global.runDelayed:${args?.get(2)}"
                consumerArgument(args, 1).accept(delayedTask)
                delayedTask
            }

            "runAtFixedRate" -> {
                calls += "global.runAtFixedRate:${args?.get(2)}:${args?.get(3)}"
                consumerArgument(args, 1).accept(repeatingTask)
                repeatingTask
            }

            "cancelTasks" -> {
                calls += "global.cancelTasks"
                null
            }

            else -> defaultValue(method.returnType)
        }
    }

    private fun asyncSchedulerProxy(
        calls: MutableList<String>,
        task: ScheduledTask,
    ): AsyncScheduler = proxy(AsyncScheduler::class.java) { method, args ->
        when (method.name) {
            "runNow" -> {
                calls += "async.runNow"
                consumerArgument(args, 1).accept(task)
                task
            }

            "cancelTasks" -> {
                calls += "async.cancelTasks"
                null
            }

            else -> defaultValue(method.returnType)
        }
    }

    private fun regionSchedulerProxy(
        calls: MutableList<String>,
        task: ScheduledTask,
    ): RegionScheduler = proxy(RegionScheduler::class.java) { method, args ->
        when (method.name) {
            "execute" -> {
                calls += "region.execute"
                (args?.get(2) as Runnable).run()
                null
            }

            "run" -> {
                calls += "region.run"
                consumerArgument(args, 2).accept(task)
                task
            }

            else -> defaultValue(method.returnType)
        }
    }

    private fun entitySchedulerProxy(
        calls: MutableList<String>,
        delayedTask: ScheduledTask,
    ): EntityScheduler = proxy(EntityScheduler::class.java) { method, args ->
        when (method.name) {
            "run" -> {
                calls += "entity.run"
                consumerArgument(args, 1).accept(delayedTask)
                delayedTask
            }

            "runDelayed" -> {
                calls += "entity.runDelayed:${args?.get(3)}"
                consumerArgument(args, 1).accept(delayedTask)
                delayedTask
            }

            else -> defaultValue(method.returnType)
        }
    }

    private fun scheduledTaskProxy(onCancel: () -> Unit): ScheduledTask =
        proxy(ScheduledTask::class.java) { method, _ ->
            when (method.name) {
                "cancel" -> {
                    onCancel()
                    null
                }

                else -> defaultValue(method.returnType)
            }
        }

    @Suppress("UNCHECKED_CAST")
    private fun consumerArgument(args: Array<out Any?>?, index: Int): java.util.function.Consumer<ScheduledTask> {
        return args?.get(index) as java.util.function.Consumer<ScheduledTask>
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
