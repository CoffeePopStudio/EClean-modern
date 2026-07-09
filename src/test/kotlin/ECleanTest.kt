package top.e404.eclean.test

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockbukkit.mockbukkit.MockBukkit
import top.e404.eclean.EClean
import clean.ChunkCleanTest
import clean.DropCleanTest
import clean.LivingCleanTest
import consoleOut
import player
import plugin
import server
import trash.TrashcanTest
import world

@DisplayName("清理单元测试")
@Disabled("MockBukkit 插件引导与当前 eplugin/JavaPlugin 加载路径不兼容，待后续统一重做测试基建")
class ECleanTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            EClean.unit = true
            server = MockBukkit.mock()
            plugin = MockBukkit.load(EClean::class.java)
            world = server.addSimpleWorld("world")
            player = server.addPlayer("mock")
            consoleOut
        }

        @JvmStatic
        @BeforeAll
        fun finalize() {
            MockBukkit.unmock()
        }
    }

    @Nested
    @DisplayName("区块清理单元测试")
    inner class TestChunkClean : ChunkCleanTest()

    @Nested
    @DisplayName("掉落物清理单元测试")
    inner class TestDropClean : DropCleanTest()

    @Nested
    @DisplayName("生物清理单元测试")
    inner class TestLivingClean : LivingCleanTest()

    @Nested
    @DisplayName("垃圾桶单元测试")
    inner class TestTrashcan : TrashcanTest()
}
