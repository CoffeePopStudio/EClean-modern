package top.e404.eclean

import org.bukkit.Bukkit
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.clean.Trashcan
import top.e404.eclean.command.Commands
import top.e404.eclean.config.Config
import top.e404.eclean.lang.MLangHost
import top.e404.eclean.feature.papi.native.ECleanPapiExpansion
import top.e404.eclean.listener.DespawnListener
import top.e404.eclean.menu.MenuManager
import top.e404.eclean.update.Update
import top.e404.eplugin.EPlugin
import java.io.File

open class EClean : EPlugin {
    companion object {
        val logo = listOf(
            "<gold> ______     ______     __         ______     ______     __   __   ",
            "<gold>/\\  ___\\   /\\  ___\\   /\\ \\       /\\  ___\\   /\\  __ \\   /\\ \"-.\\ \\  ",
            "<gold>\\ \\  __\\   \\ \\ \\____  \\ \\ \\____  \\ \\  __\\   \\ \\  __ \\  \\ \\ \\-.  \\ ",
            "<gold> \\ \\_____\\  \\ \\_____\\  \\ \\_____\\  \\ \\_____\\  \\ \\_\\ \\_\\  \\ \\_\\\\\"\\_\\\"",
            "<gold>  \\/_____/   \\/_____/   \\/_____/   \\/_____/   \\/_/\\/_/   \\/_/ \\/_/",
        )
    }

    @Suppress("UNUSED")
    constructor() : super()

    @Suppress("UNUSED")
    constructor(
        loader: JavaPluginLoader,
        description: PluginDescriptionFile,
        dataFolder: File,
        file: File
    ) : super(loader, description, dataFolder, file)

    override val debugPrefix get() = langManager["debug_prefix"]
    override val prefix get() = langManager["prefix"]

    override val bstatsId = 14312
    override var debug: Boolean
        get() = Config.current.global.debug
        set(value) {
            Config.update { it.copy(global = it.global.copy(debug = value)) }
        }
    override val langManager get() = MLangHost

    init {
        PL = this
    }

    override fun onEnable() {
        if (!unit) bstats()
        RuntimeServices.init(this)
        RuntimeServices.load()
        Commands.register()
        Update.register()
        Bukkit.getPluginManager().registerEvents(DespawnListener, this)
        Bukkit.getPluginManager().registerEvents(Trashcan, this)
        Bukkit.getPluginManager().registerEvents(MenuManager, this)
        val papi = ECleanPapiExpansion()
        if (papi.canRegister()) papi.register()
        for (line in logo) RuntimeServices.messages.info(line.replace(Regex("<[^>]+>"), ""))
        RuntimeServices.messages.info("EClean-Modern enabled. Author: 404E")
    }

    override fun onDisable() {
        RuntimeServices.shutdown()
        MenuManager.shutdown()
        RuntimeServices.messages.info("EClean-Modern disabled")
    }
}

lateinit var PL: EPlugin
    private set

internal var unit = false
