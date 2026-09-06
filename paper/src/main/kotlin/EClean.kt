package top.e404.eclean

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.command.Commands
import top.e404.eclean.config.Config
import top.e404.eclean.lang.MLang
import top.e404.eclean.listener.DespawnListener
import top.e404.eclean.menu.MenuManager
import top.e404.eclean.update.Update

open class EClean : JavaPlugin {
    companion object {
        @Volatile
        var unit = false
    }

    val prefix get() = MLang["prefix"]

    lateinit var services: RuntimeServices
        private set

    @Suppress("UNUSED")
    constructor() : super()

    init {
        PL = this
    }

    override fun onEnable() {
        if (!unit && Config.current.advanced.bStats.enabled) {
            org.bstats.bukkit.Metrics(this, 33735)
        }
        services = RuntimeServices()
        services.load()
        Commands.register()
        Update.register()
        services.commonPlatform.eventBus.register(DespawnListener)
        services.commonPlatform.eventBus.register(MenuManager)
        if (Config.current.advanced.papi.enabled) {
            try {
                val clazz = Class.forName("top.e404.eclean.feature.papi.native.ECleanPapiExpansion")
                val papi = clazz.getDeclaredConstructor().newInstance()
                val canRegister = clazz.getMethod("canRegister").invoke(papi) as Boolean
                if (canRegister) {
                    clazz.getMethod("register").invoke(papi)
                }
            } catch (_: ReflectiveOperationException) {
                logger.info("PlaceholderAPI not found, skipping PAPI expansion registration")
            } catch (_: NoClassDefFoundError) {
                logger.info("PlaceholderAPI not found, skipping PAPI expansion registration")
            }
        }
        services.messages.info("EClean-Modern enabled. Author: 404E")
    }

    override fun onDisable() {
        services.shutdown()
        MenuManager.shutdown()
        services.messages.info("EClean-Modern disabled")
    }
}

lateinit var PL: EClean
    private set
