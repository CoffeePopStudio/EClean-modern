package top.e404.eclean.feature.cleanup.drop

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Item
import org.bukkit.inventory.meta.BookMeta
import top.e404.eclean.PL
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config
import top.e404.eclean.util.isMatch

class DropCleanupService {
    fun cleanAllWorlds(): List<DropCleanupResult> {
        val cfg = Config.config.drop
        val worlds = Bukkit.getWorlds().filterNot { world ->
            cfg.disableWorld.any { regex -> world.name matches regex }
        }
        return worlds.map(::cleanWorld)
    }

    fun cleanWorld(world: World): DropCleanupResult {
        val cfg = Config.config.drop
        val trashCfg = Config.config.trashcan
        val waitingForClean = world.entities.filterIsInstance<Item>().toMutableList()

        if (cfg.enchant) waitingForClean.removeIf { it.itemStack.itemMeta?.hasEnchants() == true }
        if (cfg.writtenBook) waitingForClean.removeIf {
            it.itemStack.type == Material.WRITABLE_BOOK &&
                    (it.itemStack.itemMeta as? BookMeta)?.hasPages() == true
        }
        if (cfg.lore) waitingForClean.removeIf { it.itemStack.itemMeta?.hasLore() == true }

        val items = mutableMapOf<String, MutableList<Item>>()
        waitingForClean.forEach {
            items.getOrPut(it.itemStack.type.name) { mutableListOf() }.add(it)
        }

        if (cfg.black) {
            items.entries.removeIf { (type, _) -> type.isMatch(cfg.match) == null }
        } else {
            items.entries.removeIf { (type, _) -> type.isMatch(cfg.match) != null }
        }

        var cleaned = 0
        if (trashCfg.enable && trashCfg.collect) {
            items.values.forEach { grouped ->
                cleaned += grouped.size
                RuntimeServices.trashcanService.collectStacks(grouped.map(Item::getItemStack))
                grouped.forEach(Item::remove)
            }
        } else {
            items.values.forEach { grouped ->
                cleaned += grouped.size
                grouped.forEach(Item::remove)
            }
        }
        PL.debug { "世界${world.name}掉落物清理完成($cleaned/${waitingForClean.size})" }
        return DropCleanupResult(cleaned = cleaned, total = waitingForClean.size)
    }
}
