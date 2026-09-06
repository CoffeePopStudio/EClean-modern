package top.e404.eclean.command

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.e404.eclean.PL
import top.e404.eclean.lang.MLang

/** Shared access checks used by command handlers and interactive actions. */
object CommandAccess {
    fun requirePermission(sender: CommandSender, permission: PermissionNode): Boolean {
        if (sender.hasPermission(permission)) return true
        PL.services.messages.send(sender, MLang["command.no_permission"])
        return false
    }

    fun requirePermissions(sender: CommandSender, vararg permissions: PermissionNode): Boolean {
        val missing = permissions.firstOrNull { !sender.hasPermission(it) } ?: return true
        return requirePermission(sender, missing)
    }

    fun requirePlayer(sender: CommandSender): Player? {
        val player = sender as? Player
        if (player != null) return player
        PL.services.messages.send(sender, MLang["command.player_only"])
        return null
    }

    fun requirePermissionAndPlayer(
        sender: CommandSender,
        permission: PermissionNode,
    ): Player? {
        if (!requirePermission(sender, permission)) return null
        return requirePlayer(sender)
    }

    fun requireWorld(sender: CommandSender, name: String): World? {
        val world = Bukkit.getWorld(name)
        if (world != null) return world
        PL.services.messages.send(sender, MLang["command.invalid.world", "world" to name])
        return null
    }
}
