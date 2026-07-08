package top.e404.eclean

import org.bukkit.Bukkit
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.clean.Trashcan
import top.e404.eclean.command.Commands
import top.e404.eclean.config.Config
import top.e404.eclean.config.Lang
import top.e404.eclean.hook.HookManager
import top.e404.eclean.hook.PapiHook
import top.e404.eclean.listener.DespawnListener
import top.e404.eclean.menu.MenuManager
import top.e404.eclean.papi.Papi
import top.e404.eclean.update.Update
import top.e404.eclean.util.color
import top.e404.eclean.util.removeColor
import top.e404.eplugin.EPlugin
import java.io.File

open class EClean : EPlugin {
    companion object {
        val logo = listOf(
            """&6 ______     ______     __         ______     ______     __   __   """.color,
            """&6/\  ___\   /\  ___\   /\ \       /\  ___\   /\  __ \   /\ "-.\ \  """.color,
            """&6\ \  __\   \ \ \____  \ \ \____  \ \  __\   \ \  __ \  \ \ \-.  \ """.color,
            """&6 \ \_____\  \ \_____\  \ \_____\  \ \_____\  \ \_\ \_\  \ \_\\"\_\""".color,
            """&6  \/_____/   \/_____/   \/_____/   \/_____/   \/_/\/_/   \/_/ \/_/""".color
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
    override val langManager by lazy { Lang }

    init {
        PL = this
    }

    override fun onEnable() {
        if (!unit) bstats()
        RuntimeServices.init(this)
        RuntimeServices.load()
        Commands.register()
        Update.register()
        HookManager.register()
        MenuManager.register()
        Bukkit.getPluginManager().registerEvents(DespawnListener, this)
        Bukkit.getPluginManager().registerEvents(Trashcan, this)
        if (PapiHook.enable) Papi.register()
        for (line in logo) RuntimeServices.messages.info(line.removeColor())
        RuntimeServices.messages.info("EClean-Modern enabled. Author: 404E")
    }

    override fun onDisable() {
        RuntimeServices.shutdown()
        MenuManager.shutdown()
        if (PapiHook.enable) Papi.unregister()
        RuntimeServices.messages.info("EClean-Modern disabled")
    }
}

lateinit var PL: EPlugin
    private set

internal var unit = false
