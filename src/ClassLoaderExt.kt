import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.lang.reflect.Constructor

/**
 * 本地class文件加载管理器
 * 自动读取class/下所有.class，全局缓存，插件可访问
 */
object LocalClassLoader {
    // 常量目录
    private const val CLASS_DIR = "class"
    private val rootDir = File(CLASS_DIR)

    // 全局公开容器：对外公开，插件可直接读取
    val loadedClassMap: MutableMap<String, Class<*>> = mutableMapOf()
    val instanceCache: MutableMap<String, Any> = mutableMapOf()

    private var classLoader: URLClassLoader? = null

    init {
        if (!rootDir.exists()) rootDir.mkdirs()
        // 初始化类加载器，绑定class目录
        val url = rootDir.toURI().toURL()
        classLoader = URLClassLoader(arrayOf(url), ClassLoader.getSystemClassLoader())
    }

    /** 递归扫描所有.class文件并加载 */
    fun loadAllLocalClasses() {
        println("\n[类加载] 开始扫描 $CLASS_DIR/ 目录")
        loadedClassMap.clear()
        instanceCache.clear()

        val classFiles = mutableListOf<File>()
        scanClassFile(rootDir, classFiles)

        if (classFiles.isEmpty()) {
            println("[类加载] 未找到任何.class文件")
            return
        }

        classFiles.forEach { file ->
            try {
                // 文件路径转全限定类名
                val className = file.absolutePath
                    .removePrefix(rootDir.absolutePath)
                    .replace(File.separator, ".")
                    .removeSuffix(".class")
                    .trimStart('.')

                val clazz = classLoader!!.loadClass(className)
                loadedClassMap[className] = clazz
                println("[类加载] 成功载入类：$className")

                // 无参构造自动实例化存入缓存（Java原生反射，无需kotlin-reflect）
                try {
                    val constructor: Constructor<*> = clazz.getDeclaredConstructor()
                    constructor.isAccessible = true
                    val instance = constructor.newInstance()
                    instanceCache[className] = instance
                    println("[类加载] 自动实例化：$className")
                } catch (_: NoSuchMethodException) {
                    println("[类加载] $className 无空参构造，跳过自动实例化")
                } catch (e: Exception) {
                    println("[类加载] $className 实例化失败：${e.message}")
                }
            } catch (e: Exception) {
                println("[类加载] 加载失败 ${file.name}: ${e.message}")
            }
        }
        println("[类加载] 类全部加载完成，共${loadedClassMap.size}个类")
    }

    /** 递归遍历目录收集.class */
    private fun scanClassFile(dir: File, collector: MutableList<File>) {
        val files = dir.listFiles() ?: return
        files.forEach { f ->
            if (f.isDirectory) {
                scanClassFile(f, collector)
            } else if (f.extension == "class") {
                collector.add(f)
            }
        }
    }

    // ========== 对外公开工具方法（插件可直接调用） ==========
    fun getClassByName(className: String): Class<*>? = loadedClassMap[className]

    @Suppress("UNCHECKED_CAST")
    fun <T> getInstanceByName(className: String): T? {
        return instanceCache[className] as T?
    }

    // 动态创建实例（插件按需手动创建，不走缓存）
    fun newInstance(className: String): Any? {
        val clazz = getClassByName(className) ?: return null
        return try {
            val ctor = clazz.getDeclaredConstructor()
            ctor.isAccessible = true
            ctor.newInstance()
        } catch (e: Exception) {
            println("[类加载] 动态创建 $className 实例失败：${e.message}")
            null
        }
    }
}
