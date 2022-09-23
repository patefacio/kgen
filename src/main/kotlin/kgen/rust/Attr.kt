package kgen.rust

import kgen.Id
import kgen.id

sealed class Attr(val id: Id) : AsRust {
    class Word(id: Id) : Attr(id) {
        constructor(id: String) : this(id(id))

        override val asRust: String
            get() = "#[${id.snakeCaseName}]"
    }

    class Value(id: Id, val value: String) : Attr(id) {
        constructor(id: String, value: String) : this(id(id), value)

        override val asRust: String
            get() = "#[${id.snakeCaseName} = \"$value\"]"

    }

    class Words(id: Id, val words: List<Id>) : Attr(id) {
        constructor(id: String, words: List<String>) : this(id(id), words.map { id(it) })
        constructor(id: String, vararg words: String) : this(id(id), words.map { id(it) })

        override val asRust: String
            get() = "#[${id.snakeCaseName}(${words.map { it.snakeCaseName }.joinToString(", ")})]"
    }

    class Dict(id: Id, val dict: Map<Id, String>) : Attr(id) {
        constructor(id: String, words: Map<String, String>) : this(id(id), words.map { (k, v) -> id(k) to v }.toMap())
        constructor(id: String, vararg words: Pair<String, String>) : this(
            id(id),
            words.map { (k, v) -> id(k) to v }.toMap()
        )

        override val asRust: String
            get() = "#[${id.snakeCaseName}(${
                dict.entries
                    .map { (k, v) -> "${k.snakeCaseName} = \"${v}\"" }
                    .joinToString(", ")
            })]"
    }

}

val List<Attr>.asRust
    get() = if (this.isEmpty()) {
        ""
    } else {
        this.joinToString("\n") { it.asRust }
    }

interface AttrList : AsRust {
    val attrs: List<Attr>
    override val asRust: String
        get() = attrs.asRust
}
