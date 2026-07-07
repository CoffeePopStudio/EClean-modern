package top.e404.eclean.platform

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class FoliaSchedulerFacade(
    private val plugin: Plugin
) : SchedulerFacade {
    private val fallback = PaperSchedulerFacade(plugin)

    override fun runGlobal(task: () -> Unit) {
        val scheduler = plugin.server.javaClass.getMethod("getGlobalRegionScheduler").invoke(plugin.server)
        val execute = scheduler.javaClass.methods.firstOrNull {
            it.name == "execute" && it.parameterTypes.size == 2
        }
        if (execute != null) {
            execute.invoke(scheduler, plugin, Runnable(task))
            return
        }
        val run = scheduler.javaClass.methods.firstOrNull {
            it.name == "run" && it.parameterTypes.size == 2
        }
        if (run != null) {
            run.invoke(scheduler, plugin, consumer(task))
            return
        }
        fallback.runGlobal(task)
    }

    override fun runAsync(task: () -> Unit) {
        val scheduler = plugin.server.javaClass.getMethod("getAsyncScheduler").invoke(plugin.server)
        val runNow = scheduler.javaClass.methods.firstOrNull {
            it.name == "runNow" && it.parameterTypes.size == 2
        }
        if (runNow != null) {
            runNow.invoke(scheduler, plugin, consumer(task))
            return
        }
        fallback.runAsync(task)
    }

    override fun runAtLocation(location: Location, task: () -> Unit) {
        val scheduler = plugin.server.javaClass.getMethod("getRegionScheduler").invoke(plugin.server)
        val execute = scheduler.javaClass.methods.firstOrNull {
            it.name == "execute" && it.parameterTypes.size == 3
        }
        if (execute != null) {
            execute.invoke(scheduler, plugin, location, Runnable(task))
            return
        }
        val run = scheduler.javaClass.methods.firstOrNull {
            it.name == "run" && it.parameterTypes.size == 3
        }
        if (run != null) {
            run.invoke(scheduler, plugin, location, consumer(task))
            return
        }
        fallback.runAtLocation(location, task)
    }

    override fun runForEntity(entity: Entity, task: () -> Unit) {
        val scheduler = entity.javaClass.getMethod("getScheduler").invoke(entity)
        val run = scheduler.javaClass.methods.firstOrNull {
            it.name == "run" && it.parameterTypes.size == 3
        }
        if (run != null) {
            run.invoke(scheduler, plugin, consumer(task), Runnable { })
            return
        }
        fallback.runForEntity(entity, task)
    }

    override fun runLaterGlobal(delayTicks: Long, task: () -> Unit): SchedulerHandle? {
        val scheduler = plugin.server.javaClass.getMethod("getGlobalRegionScheduler").invoke(plugin.server)
        val delayed = scheduler.javaClass.methods.firstOrNull {
            it.name == "runDelayed" && it.parameterTypes.size == 3
        } ?: return fallback.runLaterGlobal(delayTicks, task)
        return handle(delayed.invoke(scheduler, plugin, consumer(task), delayTicks))
    }

    override fun runLaterForEntity(entity: Entity, delayTicks: Long, task: () -> Unit): SchedulerHandle? {
        val scheduler = entity.javaClass.getMethod("getScheduler").invoke(entity)
        val delayed = scheduler.javaClass.methods.firstOrNull {
            it.name == "runDelayed" && it.parameterTypes.size == 4
        } ?: return fallback.runLaterForEntity(entity, delayTicks, task)
        return handle(delayed.invoke(scheduler, plugin, consumer(task), Runnable { }, delayTicks))
    }

    override fun scheduleRepeatingGlobal(delayTicks: Long, periodTicks: Long, task: () -> Unit): SchedulerHandle? {
        val scheduler = plugin.server.javaClass.getMethod("getGlobalRegionScheduler").invoke(plugin.server)
        val repeating = scheduler.javaClass.methods.firstOrNull {
            it.name == "runAtFixedRate" && it.parameterTypes.size == 4
        }
        if (repeating != null) {
            return handle(repeating.invoke(scheduler, plugin, consumer(task), delayTicks, periodTicks))
        }

        val asyncScheduler = plugin.server.javaClass.getMethod("getAsyncScheduler").invoke(plugin.server)
        val asyncRepeating = asyncScheduler.javaClass.methods.firstOrNull {
            it.name == "runAtFixedRate" && it.parameterTypes.size == 5
        }
        if (asyncRepeating != null) {
            return handle(asyncRepeating.invoke(asyncScheduler, plugin, consumer(task), delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS))
        }

        return fallback.scheduleRepeatingGlobal(delayTicks, periodTicks, task)
    }

    private fun consumer(task: () -> Unit): Consumer<Any> = Consumer { task() }

    private fun handle(rawTask: Any?): SchedulerHandle? {
        if (rawTask == null) return null
        val cancel = rawTask.javaClass.methods.firstOrNull { it.name == "cancel" && it.parameterTypes.isEmpty() }
            ?: return null
        return SchedulerHandle { cancel.invoke(rawTask) }
    }
}
