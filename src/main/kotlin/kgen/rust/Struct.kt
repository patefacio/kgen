package kgen.rust

import kgen.Id

data class Struct(
    val id: Id,
    val doc: String = "TODO: DOCUMENT Struct(${id.snakeCaseName})",
    val fields: List<Field> = emptyList(),
    val visibility: Visibility = Visibility.None
) : Type {
    override val type: String
        get() = TODO("Not yet implemented")
}