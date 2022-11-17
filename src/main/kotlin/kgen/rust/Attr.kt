package kgen.rust

import kgen.Id
import kgen.Identifier
import kgen.id

interface AsAttr {
    val asInnerAttr: String
    val asOuterAttr: String
}

sealed class Attr(id: Id) : Identifier(id), AsAttr {
    class Word(nameId: String) : Attr(id(nameId)) {

        override val asInnerAttr: String
            get() = "#![${id.snakeCaseName}]"

        override val asOuterAttr: String
            get() = "#[${id.snakeCaseName}]"
    }

    class Value(nameId: String, val value: String) : Attr(id(nameId)) {
        override val asInnerAttr: String
            get() = "#![${id.snakeCaseName} = \"$value\"]"
        override val asOuterAttr: String
            get() = "#[${id.snakeCaseName} = \"$value\"]"
    }

    class Words(nameId: String, val words: List<String>) : Attr(id(nameId)) {
        constructor(nameId: String, vararg words: String) : this(nameId, words.toList())

        override val asInnerAttr: String
            get() = "#![${id.snakeCaseName}(${words.joinToString(", ")})]"
        override val asOuterAttr: String
            get() = "#[${id.snakeCaseName}(${words.joinToString(", ")})]"
    }

    class Dict(nameId: String, val dict: Map<Id, String?>) : Attr(id(nameId)) {
        constructor(nameId: String, vararg words: Pair<String, String?>) : this(
            nameId,
            words.associate { (k, v) -> id(k) to v }
        )

        constructor(nameId: String, words: List<Pair<String, String?>>) : this(
            nameId,
            words.associate { (k, v) -> id(k) to v }
        )

        private val attrDecl get() = dict.entries
            .map { (k, v) ->
                if (v == null) {
                    k.snakeCaseName
                } else {
                    "${k.snakeCaseName} = \"${v}\""
                }
            }
            .joinToString(", ")

        override val asInnerAttr: String
            get() = "#![${id.snakeCaseName}($attrDecl)]"

        override val asOuterAttr: String
            get() = "#[${id.snakeCaseName}($attrDecl)]"
    }

}

val List<Attr>.asOuterAttr
    get() = if (this.isEmpty()) {
        ""
    } else {
        this.joinToString("\n") { it.asOuterAttr }
    }

val List<Attr>.asInnerAttr
    get() = if (this.isEmpty()) {
        ""
    } else {
        this.joinToString("\n") { it.asInnerAttr }
    }

val attrCfgTest = Attr.Words("cfg", "test")
val aggrSerdeSerialize = Attr.Words("derive", "Serialize", "Deserialize")
val attrTestFn = Attr.Word("test")


