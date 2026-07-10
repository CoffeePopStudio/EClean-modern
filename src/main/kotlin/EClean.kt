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

    val debugPrefix get() = MLang["debug_prefix"]
    val prefix get() = MLang["prefix"]
    val debuggers = mutableSetOf<String>()

    var debug: Boolean
        get() = Config.current.global.debug
        set(value) {
            Config.update { it.copy(global = it.global.copy(debug = value)) }
        }

    @Suppress("UNUSED")
    constructor() : super()

    init {
        PL = this
    }

    override fun onEnable() {
        if (!unit) {
            org.bstats.bukkit.Metrics(this, 14312)
        }
        RuntimeServices.init(this)
        RuntimeServices.load()
        Commands.register()
        Update.register()
        Bukkit.getPluginManager().registerEvents(DespawnListener, this)
        Bukkit.getPluginManager().registerEvents(MenuManager, this)
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
        RuntimeServices.messages.info("EClean-Modern enabled. Author: 404E")
    }

    override fun onDisable() {
        RuntimeServices.shutdown()
        MenuManager.shutdown()
        RuntimeServices.messages.info("EClean-Modern disabled")
    }
}

lateinit var PL: EClean
    private set
