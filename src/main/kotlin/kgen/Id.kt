package kgen

data class Id(val snakeCaseName: String) {
    val capCamel get() = capCamel(snakeCaseName)
    val snake get() = snakeCaseName
    val emacs get() = emacs(snakeCaseName)
    val shout get() = snakeCaseName.uppercase()

    val title get() = capCamel(snakeCaseName, " ")

    val shoutAbbrev get() = abbrev(snakeCaseName).uppercase()

    override fun toString() = snake
}

fun id(snakeCaseName: String) = when (isSnake(snakeCaseName)) {
    true -> Id(snakeCaseName)
    else -> throw IllegalArgumentException("`$snakeCaseName`: must be snake case!")
}

val String.asId get() = Id(this.asSnake)

val List<String>.asId get() = id(this.joinToString("_"))