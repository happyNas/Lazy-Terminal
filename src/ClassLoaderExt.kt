import java.io.File
import java.lang.reflect.Constructor
import java.net.URLClassLoader

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
        Log.log("info", "Start to load all classes.")
        loadedClassMap.clear()
        instanceCache.clear()

        val classFiles = mutableListOf<File>()
        scanClassFile(rootDir, classFiles)

        if (classFiles.isEmpty()) {return}

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
                Log.log("info", "Loaded class：$className.")

                // 无参构造自动实例化存入缓存（Java原生反射，无需kotlin-reflect）
                try {
                    val constructor: Constructor<*> = clazz.getDeclaredConstructor()
                    constructor.isAccessible = true
                    val instance = constructor.newInstance()
                    instanceCache[className] = instance
                } catch (_: NoSuchMethodException) {} catch (e: Exception) {
                    Log.log("error", "$className has some problems.")
                }
            } catch (e: Exception) {
                Log.log("error", "Can not load ${file.name}: ${e.message}")
            }
        }
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
            Log.log("error", "$className has some problems.")
            null
        }
    }
}
