package top.e404.eclean.test

import be.seeseemelk.mockbukkit.MockBukkit
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import top.e404.eclean.EClean
import top.e404.eclean.test.clean.ChunkCleanTest
import top.e404.eclean.test.clean.DropCleanTest
import top.e404.eclean.test.clean.LivingCleanTest
import top.e404.eclean.unit
import trash.TrashcanTest
import java.io.File

@DisplayName("清理单元测试")
@Disabled("MockBukkit 插件引导与当前 eplugin/JavaPlugin 加载路径不兼容，待后续统一重做测试基建")
class ECleanTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            unit = true
            server = MockBukkit.mock()
            val descriptionStream = checkNotNull(EClean::class.java.classLoader.getResourceAsStream("plugin.yml")) {
                "测试环境未找到 plugin.yml"
            }
            descriptionStream.use { input ->
                val loader = JavaPluginLoader(server)
                val description = PluginDescriptionFile(input)
                val dataFolder = File("build/mockbukkit/eclean-test-data")
                val pluginFile = checkNotNull(
                    File("build/libs").listFiles()
                        ?.filter { it.isFile && it.extension == "jar" }
                        ?.maxByOrNull(File::lastModified)
                ) { "测试环境未找到插件 jar" }
                plugin = MockBukkit.loadWith(
                    EClean::class.java,
                    pluginFile,
                    loader,
                    description,
                    dataFolder,
                    pluginFile,
                )
            }
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
