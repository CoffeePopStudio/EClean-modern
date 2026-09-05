package top.e404.eclean.paper.adapt

import org.bukkit.Bukkit
import top.e404.eclean.common.api.ServerInfo

class PaperServerInfo : ServerInfo {
    override val worldNames: List<String>
        get() = Bukkit.getWorlds().map { it.name }

    override val onlinePlayerIds: List<String>
        get() = Bukkit.getOnlinePlayers().map { it.uniqueId.toString() }

    override val onlinePlayerCount: Int
        get() = Bukkit.getOnlinePlayers().size
}
