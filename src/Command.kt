interface Command {
    val name: String
    fun execute(args: Array<String>)
}