package kgen

/** An item with an id */
interface Identifiable {
    val id: Id
}

/** Base clase for items identifyable by name */
open class Identifier(override val id: Id) : Identifiable {
    constructor(nameId: String) : this(id(nameId))
}