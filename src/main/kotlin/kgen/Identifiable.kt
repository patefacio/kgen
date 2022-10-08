package kgen

import kgen.Id
import kgen.id

open class Identifiable(val id: Id) {
    constructor(nameId: String) : this(id(nameId))
}