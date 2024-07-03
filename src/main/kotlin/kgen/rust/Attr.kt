package kgen.rust

import kgen.*

/** An element as a rust attribute */
interface AsAttr {
    val asInnerAttr: String
    val asOuterAttr: String
}

/** A [rust attribute](https://doc.rust-lang.org/reference/attributes.html) */
sealed class Attr(id: Id) : Identifier(id), AsAttr {

    data class Text(val text: String) : Attr(id("text")) {

        override val asInnerAttr: String
            get() = "#![$text]"

        override val asOuterAttr: String
            get() = "#[$text]"
    }

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

/** Models an attribute dictionary value.
 * Enumerates String dictionary values which are double-quoted
 * and literal dictionary values which are taken directly.
 *
 */
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

/** An attribute's text as an `outer` attribute. Outer attributes decorate the
 * following rust item, such as function, let binding, statement, function parameter,
 * etc.
 */
val List<Attr>.asOuterAttr
    get() = if (this.isEmpty()) {
        ""
    } else {
        this.coalesced.joinToString("\n") { it.asOuterAttr }
    }

/** An attribute's text as an `inner` attribute. Inner attributes are rust's way to
 * associate an attribute from inside an element. Most commonly a way to set module
 * specific attributes from within the module.
 */
val List<Attr>.asInnerAttr
    get() = if (this.isEmpty()) {
        ""
    } else {
        this.coalesced.joinToString("\n") { it.asInnerAttr }
    }

/** `cfg(feature=\"ssr\")` attribute */
val attrSsr = Attr.Dict("cfg", "feature" to "ssr")

/** `component` leptos attribute decorating a
 * [leptos component](https://book.leptos.dev/view/03_components.html?highlight=component#documenting-components) */
val attrComponent = Attr.Word("component")

/** `cfg(test)` attribute marking item, typically a function or module, as test build specific */
val attrCfgTest = Attr.Words("cfg", "test")

/** `inline` attribute for making functions inline */
val attrInline = Attr.Word("inline")

/** `dynamic` attribute */
val attrDynamic = Attr.Word("dynamic")

/** `derive(Debug)` attribute */
val attrDeriveDebug = derive("Debug")

/** `derive(Default)` attribute */
val attrDeriveDefault = derive("Default")

/** `derive(Clone)` attribute */
val attrDeriveClone = derive("Clone")

/** `derive(Copy)` attribute */
val attrDeriveCopy = derive("Copy")

/** `derive(Eq)` attribute */
val attrDeriveEq = derive("Eq")

/** `derive(PartialEq)` attribute */
val attrDerivePartialEq = derive("PartialEq")

/** `derive(PartialEq)` attribute */
val attrDeriveBuilder = derive("Builder")

/** Both `derive(Serialize)` and `derive(Deserialize)` */
val attrSerdeSerialization = Attr.Words("derive", "Serialize", "Deserialize")

/** `cfg(debug_assertions)` mark item/code as debug build only */
val attrDebugBuild = Attr.Words("cfg", "debug_assertions")

/** `cfg(not(debug_assertions))` mark item/code as not debug build */
val attrNotDebugBuild = Attr.Words("cfg", "not(debug_assertions)")

/** `derive(EnumVariantNames)` requires _strum_macros_ */
val attrEnumVariantNames = derive("EnumVariantNames")

/** `derive(EnumIter)` requires _strum_macros_ */
val attrEnumIter = derive("EnumIter")

/** Mark a function as a `test` item as in
 *
 * ```rust
 * #[test]
 * fn test_foo_method() {
 *  ...
 * }
 * ```
 */
val attrTestFn = Attr.Word("test")

/** `feature(iter_intersperse)` */
val attrIterIntersperse = Attr.Words("feature", "iter_intersperse")

/** Allows **all** unused variables in debug mode - not a good attribute long term but can
 * clean up your bacon/build in many early stage code generation scenarios.
 * `cfg_attr(debug_assertions, allow(unused_variables))`
 */
val attrDebugUnusedVariables = Attr.Words("cfg_attr", "debug_assertions, allow(unused_variables)")

/** `#[allow(unused)]` to suppress errors of unused variables/items */
val attrAllowUnused = Attr.Words("allow", "unused")

/** `feature(variant_count)` add support for variant count:
 *
 * ```rust
 *  (0..std::mem::variant_count::<Currency>())
 * ```
 */
val attrVariantCount = Attr.Words("feature", "variant_count")

/**
 * Allow gated `is_sorted_by` function, eg
 *
 * ```rust
 *         debug_assert!(
 *             sorted_values
 *                 .clone()
 *                 .is_sorted_by(|a, b| a.value.partial_cmp(&b.value)),
 *             "New values added must be sorted"
 *         );
 * ```
 */
val attrIsSorted = Attr.Words("feature", "is_sorted")

/** Require doc comments to compile: `deny(missing_docs)` */
val attrDenyMissingDoc = Attr.Words("deny", "missing_docs")

/** Attribute indicating following procedural macro should be exported, e.g:
 *
 * ```rust
 * #[macro_export]
 * macro_rules! log_component {
 *  ...
 * }
 * ```
 */
val attrMacroExport = Attr.Word("macro_export")

/** For askama templates, prevents escape handling in template `template(escape="none")`. */
val attrNoEscapeTemplate = Attr.Dict("template", "escape" to "none")

/** For pulling in tokio runtime */
val attrTokioMain = Attr.Text("tokio::main")

/** For making a tokio test */
val attrTokioTestFn = Attr.Text("tokio::test")

/** For enabling tracing in tests using `tracing_test` */
val attrTracingTest = Attr.Text("tracing_test::traced_test")

/** Convert a single [Attr] into a list of attrs ([AttrList]). */
val Attr.asAttrList get() = AttrList(this)

/** Convert a [List] of [Attr] into an [AttrList] */
val List<Attr>.asAttrList get() = AttrList(attrs = this)


