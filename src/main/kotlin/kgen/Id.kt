package kgen

import kgen.capCamel

data class Id(val snakeCaseName: String) {
    val capCamel get() = capCamel(snakeCaseName)
    val snake get() = snakeCaseName
    val emacs get() = emacs(snakeCaseName)
}

fun id(snakeCaseName: String) = when(isSnake(snakeCaseName)) {
    true -> Id(snakeCaseName)
    else -> throw IllegalArgumentException("`$snakeCaseName`: must be snake case!")
}