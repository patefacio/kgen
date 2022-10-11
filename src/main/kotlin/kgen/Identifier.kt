package kgen

interface Identifiable {
    val id: Id
}

open class Identifier(override val id: Id) : Identifiable {
    constructor(nameId: String) : this(id(nameId))
}