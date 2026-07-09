package clean

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import top.e404.eclean.clean.cleanDrop
import top.e404.eclean.clean.lastDrop
import consoleOut
import dropItems
import resetConfig
import updateDropConfig
import world
import top.e404.eclean.ui.util.editItemMeta

abstract class DropCleanTest {

    @BeforeEach
    fun enable() {
        resetConfig()
        updateDropConfig { it.copy(enabled = true) }
    }

    @Nested
    @DisplayName("附魔物品清理设置")
    inner class TestCleanEntityWithEnchant {
        @Test
        @DisplayName("启用")
        fun onEnable() {
            // 设置为true则不清理被附魔的物品
            updateDropConfig {
                it.copy(
                    protectEnchanted = true,
                    matchers = listOf(Regex("DIAMOND.*")),
                )
            }

            val location = Location(world, 8.0, 8.0, 8.0)
            val entities = world.dropItems(location, 2) { index ->
                ItemStack(Material.DIAMOND_CHESTPLATE).apply {
                    if (index == 0) addUnsafeEnchantment(Enchantment.UNBREAKING, 1)
                }
            }

            cleanDrop()

            assert(entities.count(Entity::isValid) == 1) { "禁用清理附魔的物品时, 附魔的物品应当不被清理\n$consoleOut" }
        }

        @Test
        @DisplayName("禁用")
        fun onDisable() {
            // 设置为true则不清理被附魔的物品
            updateDropConfig {
                it.copy(
                    protectEnchanted = false,
                    matchers = listOf(Regex("DIAMOND.*")),
                )
            }

            val location = Location(world, 8.0, 8.0, 8.0)
            val entities = world.dropItems(location, 2) { index ->
                ItemStack(Material.DIAMOND_CHESTPLATE).apply {
                    if (index == 0) addUnsafeEnchantment(Enchantment.UNBREAKING, 1)
                }
            }

            cleanDrop()

            assert(entities.none(Entity::isValid)) { "禁用清理附魔的物品时, 匹配物品应当被全部清理\n$consoleOut" }
        }
    }

    @Nested
    @DisplayName("写过的书清理设置")
    inner class TestCleanWrittenBook {
        @Test
        @DisplayName("启用")
        fun onEnable() {
            // 设置为true则不清理写过的书
            updateDropConfig {
                it.copy(
                    protectWrittenBook = true,
                    matchers = listOf(Regex("WRITABLE_BOOK")),
                )
            }

            val location = Location(world, 8.0, 8.0, 8.0)
            val entities = world.dropItems(location, 2) { index ->
                ItemStack(Material.WRITABLE_BOOK).apply {
                    if (index == 0) editItemMeta {
                        this as BookMeta
                        @Suppress("DEPRECATION")
                        this.pages = mutableListOf("a", "b")
                    }
                }
            }

            cleanDrop()

            assert(entities.count(Entity::isValid) == 1) { "禁用清理写过的书时, 写过的书应当不被清理\n$consoleOut" }
        }

        @Test
        @DisplayName("禁用")
        fun onDisable() {
            // 设置为true则不清理写过的书
            updateDropConfig {
                it.copy(
                    protectWrittenBook = false,
                    matchers = listOf(Regex("WRITABLE_BOOK")),
                )
            }

            val location = Location(world, 8.0, 8.0, 8.0)
            val entities = world.dropItems(location, 2) { index ->
                ItemStack(Material.WRITABLE_BOOK).apply {
                    if (index == 0) editItemMeta {
                        this as BookMeta
                        @Suppress("DEPRECATION")
                        this.pages = mutableListOf("a", "b")
                    }
                }
            }

            cleanDrop()

            assert(entities.none(Entity::isValid)) { "禁用清理写过的书时, 匹配物品应当被全部清理\n$consoleOut" }
        }
    }

    @Nested
    @DisplayName("黑白名单设置")
    inner class TestBlackWhiteList {
        @Test
        @DisplayName("黑名单")
        fun blackList() {
            // 设置为true则按黑名单匹配(名字匹配的才清理)
            updateDropConfig {
                it.copy(
                    blacklistMode = true,
                    matchers = listOf(Regex("DIAMOND.*")),
                )
            }

            val location = Location(world, 8.0, 8.0, 8.0)
            val shouldClean = world.dropItems(location, 2) { index ->
                when (index) {
                    0 -> ItemStack(Material.DIAMOND_CHESTPLATE)
                    1 -> ItemStack(Material.DIAMOND_SWORD)
                    else -> throw Exception()
                }
            }
            val shouldNotClean = world.dropItems(location, 2) { index ->
                when (index) {
                    0 -> ItemStack(Material.STONE)
                    1 -> ItemStack(Material.BOW)
                    else -> throw Exception()
                }
            }

            cleanDrop()

            assert(shouldClean.none(Entity::isValid)) { "黑名单模式中匹配的实体应该全部清理\n$consoleOut" }
            assert(shouldNotClean.all(Entity::isValid)) { "黑名单模式中不匹配的实体应该全部不清理\n$consoleOut" }
        }

        @Test
        @DisplayName("白名单")
        fun whiteList() {
            // 设置为false则按白名单匹配(名字匹配的不清理)
            updateDropConfig {
                it.copy(
                    blacklistMode = false,
                    matchers = listOf(Regex("DIAMOND.*")),
                )
            }

            val location = Location(world, 8.0, 8.0, 8.0)
            val shouldNotClean = world.dropItems(location, 2) { index ->
                when (index) {
                    0 -> ItemStack(Material.DIAMOND_CHESTPLATE)
                    1 -> ItemStack(Material.DIAMOND_SWORD)
                    else -> throw Exception()
                }
            }
            val shouldClean = world.dropItems(location, 2) { index ->
                when (index) {
                    0 -> ItemStack(Material.STONE)
                    1 -> ItemStack(Material.BOW)
                    else -> throw Exception()
                }
            }

            cleanDrop()

            assert(shouldClean.none(Entity::isValid)) { "白名单模式中不匹配的实体应该全部清理\n$consoleOut" }
            assert(shouldNotClean.all(Entity::isValid)) { "白名单模式中匹配的实体应该全部不清理\n$consoleOut" }
        }
    }

    @Test
    @DisplayName("papi")
    fun testPapi() {
        val count = 2
        updateDropConfig { it.copy(matchers = listOf(Regex("DIAMOND"))) }

        val location = Location(world, 8.0, 8.0, 8.0)
        world.dropItems(location, count) { _ -> ItemStack(Material.DIAMOND) }

        cleanDrop()

        assert(lastDrop == count) { "papi展示最后一次掉落物清理的实体数时不正确\n$consoleOut" }
    }
}
