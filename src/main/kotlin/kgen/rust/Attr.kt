package kgen.rust

import kgen.*

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
            get() = dict.entries.map { (k, v) ->
                when (v) {
                    null -> k.snakeCaseName
                    is String -> "${k.snakeCaseName}=${doubleQuote(v)}"
                    is Char -> "${k.snakeCaseName}=${charQuote(v)}"
                    is DictValue.StringValue -> "${k.snakeCaseName}=${doubleQuote(v.value)}"
                    is DictValue.LiteralValue -> "${k.snakeCaseName}=${v.value}"
                    else -> v.toString()
                }
            }.sortedBy { it }.joinToString(", ")

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

/**
 * Given list of attributes, checks for multiple dictionary attributes that
 * could be combined into one. For example:
 * ```
 * #[template(escape = "none")]
 * #[derive(Template)]
 * #[template(path = "view_static_page.html")]
 * pub struct ViewStaticPage<'a> {
 *    ...
 * ```
 *
 * In this case the two `template` dict attrs should be joined
 */
val List<Attr>.coalesced
    get(): List<Attr> {
        val (dicts, nonDicts) = this.partition { it is Attr.Dict }
        val matchingDicts = dicts.fold(mutableMapOf<String, Map<Id, Any?>>()) { acc, attr ->
            val dictAttr = attr as Attr.Dict
            acc.merge(attr.id.snake, dictAttr.dict) { mergedDict, newDict ->
                mergedDict + newDict
            }

            acc
        }

        val all = (matchingDicts.map { (nameId, dict) ->
            Attr.Dict(nameId, dict)
        } + nonDicts)/* TODO: rethink sorting these .sortedBy { it.id.snake }*/

        // Order attrs to put `derive` attrs ahead of others to avoid this nonsense
        // warning: derive helper attribute is used before it is introduced
        //  --> plus_forecast/src/askama/distribution_spec_table.rs:12:3
        //   |
        //12 | #[template(path = "distribution_spec_table.html")]
        //   |   ^^^^^^^^
        //13 | #[derive(Template)]
        //   |          -------- the attribute is introduced here
        //   |
        //   = note: `#[warn(legacy_derive_helpers)]` on by default
        //   = warning: this was previously accepted by the compiler but is being phased out; it will become a hard error in a future release!
        //   = note: for more information, see issue #79202 <https://github.com/rust-lang/rust/issues/79202>
        val (derives, nonDerives) = all.partition { it is Attr.Words && it.id.snake == "derive" }
        return derives + nonDerives
    }

val List<Attr>.asOuterAttr
    get() = if (this.isEmpty()) {
        ""
    } else {
        this.coalesced.joinToString("\n") { it.asOuterAttr }
    }

val List<Attr>.asInnerAttr
    get() = if (this.isEmpty()) {
        ""
    } else {
        this.coalesced.joinToString("\n") { it.asInnerAttr }
    }

val attrCfgTest = Attr.Words("cfg", "test")
val attrInline = Attr.Word("inline")
val attrDynamic = Attr.Word("dynamic")
val attrComponent = Attr.Word("component")

val aggrSerdeSerialize = Attr.Words("derive", "Serialize", "Deserialize")
val attrDebugBuild = Attr.Words("cfg", "debug_assertions")
val attrNotDebugBuild = Attr.Words("cfg", "not(debug_assertions)")
val attrTestFn = Attr.Word("test")

val attrIterIntersperse = Attr.Words("feature", "iter_intersperse")
val attrUnusedVariables = Attr.Words("cfg_attr", "debug_assertions, allow(unused_variables)")
val attrAllowUnused = Attr.Words("allow", "unused")
val attrVariantCount = Attr.Words("feature", "variant_count")

val attrNoEscapeTemplate = Attr.Dict("template", "escape" to "none")


val Attr.asAttrList get() = AttrList(this)
val List<Attr>.asAttrList get() = AttrList(attrs = this)


