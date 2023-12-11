package kgen.rust.leptos

import kgen.abbrev
import kgen.asId
import kgen.emptyOpenDelimitedBlock
import kgen.indent
import kgen.rust.*
import kgen.rust.Enum
import kgen.rust.Visibility
import kotlinx.css.*

/** Create a leptos component
 *
 * Generates a leptos fn configured with provided properties.
 *
 * @property name Name of the component
 * @property doc Doc included in the generated leptos fn
 * @property fnParams Parameters to the component function
 * @property genericParamSet Generic associated with the function
 * @property structs structs defined in support of the component
 * @property enums enums defined in support of the component
 * @property functions functions defined in support of the component
 * @property traits traits defined in support of the component
 * @property traitImpls traitImpls defined in support of the component
 * @property consts Constants defined in support of the component
 * @property lets Top level let bindings in support of the component
 * @property traitImpls traitImpls defined in support of the component
 * @property uses Uses required for the component
 * @property typeAliases Type aliases defined in support of the component
 * @property abbrev  An abbreviation used to define a css class for the component.
 *                   All components should have a unique abbreviation and the default
 *                   is the cap letters in the capCamel of the id. In a large project
 *                   there may be conflicts so the user can specify.
 * @property i18nStrings  Map of i18n strings with key to default text **or** pointer to
 *                        other text in the fluent system.
 * @property includeView  If true includes the view at the end of the function with the
 *                        single div having the abbrev css class defined and a protect
 *                        block to put markup into.
 * @property innerHtml  If set, the view provided with a single div and inner html - no
 *                      protect block provided.
 */
data class Component(
    val name: String,
    val doc: String,
    val fnParams: List<FnParam> = emptyList(),
    val genericParamSet: GenericParamSet? = null,
    val structs: List<Struct> = emptyList(),
    val enums: List<Enum> = emptyList(),
    val functions: List<Fn> = emptyList(),
    val traits: List<Trait> = emptyList(),
    val traitImpls: List<TraitImpl> = emptyList(),
    val uses: Set<Use> = emptySet(),
    val typeAliases: List<TypeAlias> = emptyList(),
    val consts: List<Const> = emptyList(),
    val lets: List<Let> = emptyList(),
    val abbrev: String = abbrev(name),
    val i18nStrings: Map<String, String> = emptyMap(),
    val excludedStrings: Set<String> = emptySet(),
    val excludeAppContext: Boolean = false,
    val excludeLangSelect: Boolean = false,
    val includeView: Boolean = false,
    val styleLambda: String? = null,
    val cssMaxWidth: String = "var(--plus-max-width)",
    val innerHtml: String? = null,
    val cssClasses: List<String> = emptyList()
) {

    val id = name.asId

    private val allUses = listOf(
        "leptos::component",
        "leptos::view",
        "leptos::IntoView"
    ).asUses + uses

    private val selfClass = "plus-${abbrev.asId.emacs}"

    private val allCssClasses = listOf(selfClass) + cssClasses

    var cssBuilder = CssBuilder(indent = "    ").apply {
        val selector = ".$selfClass"
        rule(selector) {
            maxWidth = LinearDimension(cssMaxWidth)
        }
    }

    private val selfClassConst: Const?
        get() = if (includeView) {
            Const(
                "self_class",
                doc = null,
                "&str".asType,
                value = allCssClasses.joinToString("; ")
            )
        } else {
            null
        }
    private val preBlock: String?
        get() = """
        let component_id = crate::component_id!("`${name.asId.capCamel}`");
        #[cfg(debug_assertions)]
        crate::log_component!(crate::COMPONENT_LOG_LEVEL, component_id);
    """.trimIndent()

    private val viewStyle = if (styleLambda != null) {
        " style=$styleLambda"
    } else {
        ""
    }

    private val emptyContents get() = "\n<h5>\"TODO\"</h5>\n"

    val view: String?
        get() = when {
            innerHtml != null -> "view! { <div class=SELF_CLASS$viewStyle inner_html=$innerHtml></div> }"

            includeView -> """
view! {
    <div class=SELF_CLASS$viewStyle>
${indent(emptyOpenDelimitedBlock("$selfClass-view", emptyContents = emptyContents), "        ")}
    </div>
}        
        """.trimIndent()

            else -> null
        }

    private val langSelectLet = if (excludeLangSelect || i18nStrings.isEmpty()) {
        null
    } else {
        Let(
            "lang_selector",
            rhs = "use_context::<AppContext>().unwrap().lang_selector"
        )
    }

    private val asFn: Fn
        get() = Fn(
            name,
            doc,
            fnParams,
            returnDoc = "View for $name",
            returnType = "impl IntoView".asType,
            nameCapCamel = true,
            genericParamSet = genericParamSet,
            inlineParamDoc = true,
            attrs = attrComponent.asAttrList,
            visibility = Visibility.PubExport,
            body = FnBody(emptyBlockName = "fn $name", preBlock = preBlock, postBlock = view),
            uses = listOfNotNull(
                if (includeView) {
                    Use("leptos::IntoAttribute", allowUnusedImports = true).asUses
                } else {
                    null
                },
                if (i18nStrings.isEmpty()) {
                    null
                } else {
                    listOf(
                        "crate::AppContext",
                        "leptos::use_context",
                        "leptos::SignalGet"
                    ).asUses
                }
            ).flatten().toSet() + uses,
            consts = listOfNotNull(selfClassConst) + consts,
            localUses = if (i18nStrings.isNotEmpty()) {
                listOf("plus_lookup::i18n::${id.snakeCaseName}::*").asUses
            } else {
                emptySet()
            },
            lets = listOfNotNull(langSelectLet) + i18nStrings
                .keys
                .filter { !excludedStrings.contains(it) }.map {
                    val stringId = it.asId
                    Let(
                        "i18n_$stringId",
                        "move || i18n_${stringId}(lang_selector.get())"
                    )
                } + lets
        )

    fun withCssBuilder(rule: (CssBuilder, String) -> CssBuilder): Component {
        cssBuilder = rule(cssBuilder, ".$selfClass")
        return this
    }

    val asModule
        get() = Module(
            name,
            doc = "Module for $name leptos function/component",
            functions = listOf(
                asFn
            ) + functions,
            structs = structs,
            enums = enums,
            uses = allUses,
            typeAliases = typeAliases,
            traits = traits,
            traitImpls = traitImpls
        )
}