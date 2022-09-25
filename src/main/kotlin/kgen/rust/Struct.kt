package kgen.rust

import kgen.Id
import kgen.id

data class Struct(
    val nameId: String,
    val doc: String = "TODO: DOCUMENT Struct($nameId)",
    val fields: List<Field> = emptyList(),
    val visibility: Visibility = Visibility.None
) : Type {

    val id: Id = id(nameId)

    override val type: String
        get() = TODO("Not yet implemented")
}