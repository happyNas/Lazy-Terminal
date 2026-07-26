fun main() {
    LocalClassLoader.loadAllLocalClasses()
    PluginLoader.loadPlugins()
    Terminal.start()
}
