/**
 * 统一命令注册中心（修复：与PluginManager职责重复问题）
 */
object CommandRegistry {
    private val commands = mutableMapOf<String, Command>()

    // 统一小写注册命令
    fun registerCommand(command: Command) {
        commands[command.name.lowercase()] = command
    }

    // 批量注册插件命令
    fun registerPluginCommands(commands: List<Command>) {
        commands.forEach { registerCommand(it) }
    }

    // 查找命令（小写匹配）
    fun findCommand(name: String): Command? = commands[name.lowercase()]

    // 获取所有命令
    fun getAllCommands(): Collection<Command> = commands.values
}