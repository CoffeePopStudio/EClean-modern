package clean

import consoleOut
import org.mockbukkit.mockbukkit.entity.LivingEntityMock
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import player
import resetConfig
import spawnEntities
import top.e404.eclean.clean.cleanDenseEntities
import top.e404.eclean.clean.lastChunk
import updateChunkDensityConfig
import world

abstract class ChunkCleanTest {

    @BeforeEach
    fun enable() {
        resetConfig()
        updateChunkDensityConfig { it.copy(enabled = true) }
    }

    @Nested
    @DisplayName("实体名清理设置")
    inner class TestCleanEntityWithCustomName {
        @Test
        @DisplayName("启用")
        fun onEnable() {
            // 设置为true则清理被命名的生物
            val limit = 5
            updateChunkDensityConfig {
                it.copy(
                    settings = it.settings.copy(cleanNamed = true),
                    entityLimits = mapOf(Regex("ZOMBIE") to limit),
                )
            }

            val chunk = world.getChunkAt(0, 0)
            chunk.load()
            val location = Location(world, 8.0, 8.0, 8.0)
            val entities = world.spawnEntities(location, EntityType.ZOMBIE, 16) { _, zombie ->
                @Suppress("DEPRECATION")
                zombie.customName = "custom name"
            }

            cleanDenseEntities()

            val valid = entities.count(Entity::isValid)
            assert(valid == limit) { "启用清理命名的实体时, 命名的实体应当被清理($valid != $limit)\n$consoleOut" }
        }

        @Test
        @DisplayName("禁用")
        fun onDisable() {
            // 设置为true则清理被命名的生物
            updateChunkDensityConfig {
                it.copy(
                    settings = it.settings.copy(cleanNamed = false),
                    entityLimits = mapOf(Regex("ZOMBIE") to 5),
                )
            }

            val chunk = world.getChunkAt(0, 0)
            chunk.load()
            val location = Location(world, 8.0, 8.0, 8.0)
            val entities = world.spawnEntities(location, EntityType.ZOMBIE, 16) { _, zombie ->
                @Suppress("DEPRECATION")
                zombie.customName = "custom name"
            }

            cleanDenseEntities()
            assert(entities.all(Entity::isValid)) { "禁用清理命名的实体时, 命名的实体不应当被清理\n$consoleOut" }
        }
    }

    @Nested
    @DisplayName("拴绳清理设置")
    inner class TestCleanEntityWithLead {
        @Test
        @DisplayName("启用")
        fun onEnable() {
            // 设置为true则清理被拴绳拴住的生物
            val limit = 5
            updateChunkDensityConfig {
                it.copy(
                    settings = it.settings.copy(cleanLeashed = true),
                    entityLimits = mapOf(Regex("SHEEP") to limit),
                )
            }

            val chunk = world.getChunkAt(0, 0)
            chunk.load()
            val location = Location(world, 8.0, 8.0, 8.0)
            val entities = world.spawnEntities(location, EntityType.SHEEP, 16) { _, sheep ->
                sheep as LivingEntityMock
                sheep.setLeashHolder(sheep)
            }

            cleanDenseEntities()
            val valid = entities.count(Entity::isValid)
            assert(valid == limit) { "启用清理拴绳拴住的实体时, 拴绳拴住的实体应当被清理($valid != $limit)\n$consoleOut" }
        }

        @Test
        @DisplayName("禁用")
        fun onDisable() {
            // 设置为true则清理被拴绳拴住的生物
            updateChunkDensityConfig {
                it.copy(
                    settings = it.settings.copy(cleanLeashed = false),
                    entityLimits = mapOf(Regex("SHEEP") to 5),
                )
            }

            val chunk = world.getChunkAt(0, 0)
            chunk.load()
            val location = Location(world, 8.0, 8.0, 8.0)
            val entities = world.spawnEntities(location, EntityType.SHEEP, 16) { _, sheep ->
                sheep as LivingEntityMock
                sheep.setLeashHolder(sheep)
            }

            cleanDenseEntities()
            assert(entities.all(Entity::isValid)) { "禁用清理拴绳拴住的实体时, 拴绳拴住的实体不应当被清理\n$consoleOut" }
        }
    }

    @Nested
    @DisplayName("乘骑清理设置")
    inner class TestCleanEntityWithMount {
        @Test
        @DisplayName("启用")
        fun onEnable() {
            // 设置为true则清理乘骑中的生物
            val limit = 5
            updateChunkDensityConfig {
                it.copy(
                    settings = it.settings.copy(cleanMounted = true),
                    entityLimits = mapOf(Regex("HORSE") to limit),
                )
            }

            val chunk = world.getChunkAt(0, 0)
            chunk.load()
            val location = Location(world, 8.0, 8.0, 8.0)
            val entities = world.spawnEntities(location, EntityType.HORSE, 16) { index, horse ->
                horse as LivingEntityMock
                if (index < 8) horse.addPassenger(player)
                else player.addPassenger(horse)
            }

            cleanDenseEntities()
            val valid = entities.count(Entity::isValid)
            assert(valid == limit) { "启用清理乘骑中的实体时, 乘骑中的实体应当被清理($valid != $limit)\n$consoleOut" }
        }

        @Test
        @DisplayName("禁用")
        fun onDisable() {
            // 设置为true则清理乘骑中的生物
            updateChunkDensityConfig {
                it.copy(
                    settings = it.settings.copy(cleanMounted = false),
                    entityLimits = mapOf(Regex("HORSE") to 5),
                )
            }

            val chunk = world.getChunkAt(0, 0)
            chunk.load()
            val location = Location(world, 8.0, 8.0, 8.0)
            val entities = world.spawnEntities(location, EntityType.HORSE, 16) { index, horse ->
                horse as LivingEntityMock
                if (index < 8) horse.addPassenger(player)
                else player.addPassenger(horse)
            }

            cleanDenseEntities()
            assert(entities.all(Entity::isValid)) { "禁用清理乘骑中的实体时, 乘骑中的实体不应当被清理\n$consoleOut" }
        }
    }

    @Test
    @DisplayName("papi")
    fun testPapi() {
        val limit = 5
        val count = 16
        updateChunkDensityConfig {
            it.copy(entityLimits = mapOf(Regex("ZOMBIE") to limit))
        }

        val chunk = world.getChunkAt(0, 0)
        chunk.load()
        val location = Location(world, 8.0, 8.0, 8.0)
        val entities = world.spawnEntities(location, EntityType.ZOMBIE, count)

        cleanDenseEntities()
        val valid = entities.count(Entity::isValid)
        assert(valid == limit)
        assert(lastChunk == count - limit) { "papi展示最后一次区块清理的实体数时不正确\n$consoleOut" }
    }
}
