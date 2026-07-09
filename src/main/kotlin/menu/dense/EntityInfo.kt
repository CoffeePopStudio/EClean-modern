package top.e404.eclean.menu.dense

import org.bukkit.Material
import org.bukkit.entity.EntityType
import top.e404.eclean.clean.info
import top.e404.eclean.lang.MLang
import top.e404.eclean.platform.execution.ChunkRef
import top.e404.eclean.util.placeholder
import top.e404.eplugin.menu.Displayable
import top.e404.eplugin.util.buildItemStack

class EntityInfo(
    val type: EntityType,
    val amount: Int,
    val chunk: ChunkRef
) : Displayable {
    private companion object {
        val materials = Material.values().filter { it.name.contains("WOOL") }
    }

    override fun update() {}
    override var needUpdate = false
    override val item = run {
        val placeholder = arrayOf<Pair<String, Any?>>(
            "type" to type.name,
            "amount" to amount,
            "chunk" to chunk.info(),
        )
        buildItemStack(
            materials.random(),
            1,
            MLang.get("menu.dense.item.name", *placeholder),
            MLang["menu.dense.item.lore"].placeholder(*placeholder).lines()
        )
    }
}
