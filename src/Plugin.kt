interface Plugin {
    fun getName(): String
    fun getVersion(): String
    // 修复：强制非空，避免空指针
    fun getCommands(): List<Command> = emptyList()
}