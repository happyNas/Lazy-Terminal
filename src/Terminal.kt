import java.util.Scanner
import kotlin.system.exitProcess

object Terminal {
    // 修复：抽离硬编码常量
    private const val VERSION = "v1.0.1"
    private val scanner = Scanner(System.`in`)

    fun start() {
        println("===== Lazy Terminal $VERSION =====")
        println("输入 help 查看帮助，exit 退出程序\n")

        while (true) {
            print("> ")
            val input = scanner.nextLine().trim()
            if (input.isEmpty()) continue

            val args = input.split(" ").toTypedArray()
            runCommand(args)
        }
    }

    private fun runCommand(args: Array<String>) {
        val commandName = args[0]
        when (commandName.lowercase()) {
            "exit" -> {
                println("程序退出...")
                exitProcess(0)
            }
            "help" -> showHelp()
            "plugins" -> showPlugins()
            "info" -> showInfo()
            else -> {
                // 修复：使用统一命令中心查找
                val command = CommandRegistry.findCommand(commandName)
                if (command != null) {
                    command.execute(args)
                } else {
                    // 优化：友好提示+展示可用命令
                    println("未知命令：$commandName")
                    println("可用命令：help, plugins, info, ${CommandRegistry.getAllCommands().joinToString { it.name }}")
                }
            }
        }
    }

    private fun showHelp() {
        println("===== 帮助菜单 =====")
        println("help    - 查看帮助")
        println("info    - 查看Lazy Terminal信息")
        println("plugins - 查看已加载插件")
        println("exit    - 退出程序")
        CommandRegistry.getAllCommands().forEach {
            println(it.name)
        }
    }

    private fun showPlugins() {
        val plugins = PluginManager.getAllPlugins()
        if (plugins.isEmpty()) {
            println("未加载任何插件")
            return
        }

        println("===== 已加载插件 (${plugins.size}个) =====")
        plugins.forEachIndexed { index, plugin ->
            // 修复：绝对空安全，不会崩溃
            val commands = plugin.getCommands().joinToString { it.name }
            println("${index + 1}. ${plugin.getName()} v${plugin.getVersion()} | 命令：[$commands]")
        }
    }

    private fun showInfo() {

        println("Lazy Terminal")
        println("Version 1.0.1-hotfix1")
        println("Made by happy_Nas")
        println("Made with Kotlin(Java 25)")
        println("作者是xxs咕咕咕!")

    }
}
