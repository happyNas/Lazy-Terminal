object PluginManager {
    val plugins = mutableListOf<Plugin>()

    fun registerPlugin(plugin: Plugin) {
        plugins.add(plugin)
        // 统一交给命令中心注册
        CommandRegistry.registerPluginCommands(plugin.getCommands())
    }

    // 获取所有插件
    fun getAllPlugins(): List<Plugin> = plugins
}