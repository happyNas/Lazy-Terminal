import java.io.File
import java.net.URLClassLoader
import java.util.ServiceLoader

object PluginLoader {
    // 修复：抽离硬编码为常量
    private const val PLUGIN_DIR = "mods"
    private val dir = File(PLUGIN_DIR)

    init {
        if (!dir.exists()) dir.mkdirs()
    }

    fun loadPlugins() {
        // 修复：空安全处理
        val jarFiles = dir.listFiles { it.extension == "jar" } ?: return

        jarFiles.forEach { jar ->
            println("\n[插件加载] 正在加载：${jar.name}")
            try {
                // 修复：use自动关闭类加载器，避免资源泄漏
                URLClassLoader(arrayOf(jar.toURI().toURL()), Plugin::class.java.classLoader).use { loader ->
                    val serviceLoader = ServiceLoader.load(Plugin::class.java, loader)
                    serviceLoader.forEach { plugin ->
                        PluginManager.registerPlugin(plugin)
                        println("[插件加载] 成功加载：${plugin.getName()} v${plugin.getVersion()}")
                    }
                }
            } catch (e: Exception) {
                // 修复：完整异常捕获，不影响其他插件
                println("[插件加载] 加载失败 ${jar.name}：${e.message}")
                e.printStackTrace()
            }
        }
        println("\n[插件加载] 所有插件加载完成！")
    }
}