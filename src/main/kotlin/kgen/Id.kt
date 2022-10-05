package kgen

import kgen.capCamel

data class Id(val snakeCaseName: String) {
    val capCamel get() = capCamel(snakeCaseName)
    val snake get() = snakeCaseName
    val emacs get() = emacs(snakeCaseName)
    val shout get() = snakeCaseName.uppercase()
}

fun id(snakeCaseName: String) = when (isSnake(snakeCaseName)) {
    true -> Id(snakeCaseName)
    else -> throw IllegalArgumentException("`$snakeCaseName`: must be snake case!")
}

val List<String>.asId get() = id(this.joinToString("_"))