package kgen.rust

import kgen.Id
import kgen.id

sealed class Attr(id: Id) : Identifiable(id), AsRust {
    class Word(nameId: String) : Attr(id(nameId)) {

        override val asRust: String
            get() = "#[${id.snakeCaseName}]"
    }

    class Value(nameId: String, val value: String) : Attr(id(nameId)) {
        override val asRust: String
            get() = "#[${id.snakeCaseName} = \"$value\"]"
    }

    class Words(nameId: String, val words: List<String>) : Attr(id(nameId)) {
        constructor(nameId: String, vararg words: String) : this(nameId, words.toList())

        override val asRust: String
            get() = "#[${id.snakeCaseName}(${words.joinToString(", ")})]"
    }

    class Dict(nameId: String, val dict: Map<Id, String>) : Attr(id(nameId)) {
        constructor(nameId: String, vararg words: Pair<String, String>) : this(
            nameId,
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

val attrCfgTest = Attr.Words("cfg", "test")
val attrTestFn = Attr.Word("test")


