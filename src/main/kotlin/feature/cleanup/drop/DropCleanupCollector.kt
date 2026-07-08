package top.e404.eclean.feature.cleanup.drop

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Item
import org.bukkit.inventory.meta.BookMeta
import java.util.UUID

data class DropCleanupCandidate(
    val id: UUID,
    val type: String,
    val enchanted: Boolean,
    val lore: Boolean,
    val writtenBook: Boolean,
    val item: Item? = null,
)

data class DropCleanupCollection(
    val candidates: List<DropCleanupCandidate>,
)

class DropCleanupCollector {
    fun collect(world: World): DropCleanupCollection = DropCleanupCollection(
        candidates = world.entities.filterIsInstance<Item>().map { item ->
            DropCleanupCandidate(
                id = item.uniqueId,
                type = item.itemStack.type.name,
                enchanted = item.itemStack.itemMeta?.hasEnchants() == true,
                lore = item.itemStack.itemMeta?.hasLore() == true,
                writtenBook = item.itemStack.type == Material.WRITABLE_BOOK &&
                        (item.itemStack.itemMeta as? BookMeta)?.hasPages() == true,
                item = item,
            )
        }
    )
}
