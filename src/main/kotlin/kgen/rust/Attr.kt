package kgen.rust

import kgen.Id
import kgen.Identifier
import kgen.doubleQuote
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
            get() = "#![${id.snakeCaseName}=\"$value\"]"
        override val asOuterAttr: String
            get() = "#[${id.snakeCaseName}=\"$value\"]"
    }

    class Words(nameId: String, val words: List<String>) : Attr(id(nameId)) {
        constructor(nameId: String, vararg words: String) : this(nameId, words.toList())

        override val asInnerAttr: String
            get() = "#![${id.snakeCaseName}(${words.joinToString(", ")})]"
        override val asOuterAttr: String
            get() = "#[${id.snakeCaseName}(${words.joinToString(", ")})]"
    }

    class Dict(nameId: String, val dict: Map<Id, Any?>) : Attr(id(nameId)) {
        constructor(nameId: String, vararg words: Pair<String, Any?>) : this(
            nameId,
            words.associate { (k, v) -> id(k) to v }
        )

        constructor(nameId: String, words: List<Pair<String, Any?>>) : this(
            nameId,
            words.associate { (k, v) -> id(k) to v }
        )

        private val attrDecl
            get() = dict.entries.joinToString(", ") { (k, v) ->
                when (v) {
                    null -> k.snakeCaseName
                    is String -> "${k.snakeCaseName}=${doubleQuote(v)}"
                    is DictValue.StringValue -> "${k.snakeCaseName}=${doubleQuote(v.value)}"
                    is DictValue.LiteralValue -> "${k.snakeCaseName}=${v.value}"
                    else -> v.toString()
                }
            }

        override val asInnerAttr: String
            get() = "#![${id.snakeCaseName}($attrDecl)]"

        override val asOuterAttr: String
            get() = "#[${id.snakeCaseName}($attrDecl)]"
    }
}

sealed class DictValue {
    class StringValue(val value: String) : DictValue()
    class LiteralValue(val value: String) : DictValue()
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
val attrInline = Attr.Word("inline")
val attrDynamic = Attr.Word("dynamic")

val aggrSerdeSerialize = Attr.Words("derive", "Serialize", "Deserialize")
val attrDebugBuild = Attr.Words("cfg", "debug_assertions")
val attrNotDebugBuild = Attr.Words("cfg", "not(debug_assertions)")
val attrTestFn = Attr.Word("test")

val attrIterIntersperse = Attr.Words("feature", "iter_intersperse")
val attrUnusedVariables = Attr.Words("cfg_attr", "debug_assertions, allow(unused_variables)")


val Attr.asAttrList get() = AttrList(this)
val List<Attr>.asAttrList get() = AttrList(attrs = this)


