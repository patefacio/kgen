package kgen.rust

import kgen.*

/** Models a rust function.
 * @property nameId The snake case name of the function
 * @property doc The comment for the function
 * @property params The list of function parameters
 * @property returnType The return type for the function
 * @property returnDoc The comment associated with the returned value
 * @property inlineDecl Specify the inline aspect of the function
 * @property genericParamSet Set of generic parameters of the function
 * @property visibility Rust visibility of the function
 * @property body The function body
 * @property isTest If true treats function as test by adding test attribute
 * @property hasUnitTest If true include unit test in same module
 * @property isTokioTest If true treats function as tokio test by adding test attribute
 * @property hasTokioTest If true include tokio unit test in same module
 * @property attrs Rust attributes associated with the function
 * @property blockName Name of block for rust code of the function. Reasonable default
 *                     based on [nameId] provided, but can override.
 * @property emptyBlockContents Contents of empty block. Defaults to a `todo!(...)` so
 *           code will build and run and panic until implemented
 * @property uses Import requirements for the function
 * @property localUses Import requirements for the function's implementation. Resolves to
 *           additional using statements after the signature before the block
 * @property testNameIds For more complex functions, a list of names that will generate
 *           test functions.
 * @property panicTestNameIds For more complex functions, a list of names that will generate
 *           test functions that are expected to panic.
 * @property nameCapCamel The function name in _cap camel_
 * @property inlineParamDoc If true will generate parameter comments inline. Generally rust
 *           has no support for parameter doc comments. However, leptos components provide
 *           a macro that transforms leptos functions into a builder pattern so the comments
 *           can be used directly. This is special doc formatting for such rare cases.
 * @property consts List of constants defined at the start of the function
 * @property lets List of let bindings defined at the start of the function
 * @property isAsync If true function is defined `async`
 */
data class Fn(
    val nameId: String,
    val doc: String? = missingDoc(nameId, "Fn"),
    val params: List<FnParam> = emptyList(),
    val returnType: Type? = null,
    val returnDoc: String? = missingDoc(nameId, "FnReturn"),
    val inlineDecl: InlineDecl = InlineDecl.None,
    val genericParamSet: GenericParamSet? = null,
    val visibility: Visibility = Visibility.Pub,
    val body: FnBody? = null,
    val isTest: Boolean = false,
    val hasUnitTest: Boolean? = null,
    val isTokioTest: Boolean = false,
    val hasTokioTest: Boolean? = null,
    val attrs: AttrList = AttrList(),
    val testFnAttrs: AttrList = AttrList(),
    val blockName: String = nameId,
    val emptyBlockContents: String? = null,
    val uses: Set<Use> = emptySet(),
    val localUses: Set<Use> = emptySet(),
    val testNameIds: List<String> = emptyList(),
    val panicTestNameIds: List<String> = emptyList(),
    val nameCapCamel: Boolean = false,
    val inlineParamDoc: Boolean = false,
    val consts: List<Const> = emptyList(),
    val lets: List<Let> = emptyList(),
    val isAsync: Boolean = false
) : Identifier(nameId), AsRust {


    constructor(
        nameId: String,
        doc: String? = missingDoc(nameId, "Fn"),
        vararg params: FnParam,
        returnType: Type? = null,
        returnDoc: String? = missingDoc(nameId, "FnReturn"),
        inlineDecl: InlineDecl = InlineDecl.None,
        genericParamSet: GenericParamSet? = null,
        visibility: Visibility = Visibility.Pub,
        body: FnBody? = null,
        isTest: Boolean = false,
        hasUnitTest: Boolean? = null,
        isTokioTest: Boolean = false,
        hasTokioTest: Boolean? = null,
        attrs: AttrList = AttrList(),
        testFnAttrs: AttrList = AttrList(),
        blockName: String = nameId,
        emptyBlockContents: String? = null,
        uses: Set<Use> = emptySet(),
        localUses: Set<Use> = emptySet(),
        testNameIds: List<String> = emptyList(),
        panicTestNameIds: List<String> = emptyList(),
        nameCapCamel: Boolean = false,
        inlineParamDoc: Boolean = false,
        consts: List<Const> = emptyList(),
        lets: List<Let> = emptyList(),
        isAsync: Boolean = false,
    ) : this(
        nameId, doc, params.toList(), returnType, returnDoc, inlineDecl,
        genericParamSet, visibility, body, isTest, hasUnitTest, isTokioTest, hasTokioTest, attrs,
        testFnAttrs, blockName, emptyBlockContents, uses, localUses, testNameIds, panicTestNameIds,
        nameCapCamel, inlineParamDoc, consts, lets, isAsync
    )

    val allUses get() = uses + params.map { it.uses }.flatten().toSet()

    private val allAttrs = listOfNotNull(
        attrs,
        if (inlineDecl != InlineDecl.None) {
            AttrList(inlineDecl.asRust())
        } else {
            null
        },
        when {
            isTokioTest -> AttrList(attrTokioTestFn)
            isTest -> AttrList(attrTestFn)
            else -> null
        }
    ).reduce { acc, attrList -> acc + attrList }

    private val paramText
        get() = if (params.isEmpty()) {
            "()"
        } else {
            listOf(
                "(",
                indent(params.joinToString(",\n") {
                    if (inlineParamDoc) {
                        listOf(commentTriple(it.doc), it.asRust).joinToString("\n")
                    } else {
                        it.asRust
                    }
                }),
                ")"
            ).joinToString("\n")
        }

    private val sigReturnType
        get() = if (returnType != null) {
            " -> ${returnType.asRust}"
        } else {
            ""
        }

    private val visibilityDecl
        get() = if (!isTest) {
            visibility.asRust
        } else {
            ""
        }

    val rustFunctionName
        get() = if (nameCapCamel) {
            nameId.asId.capCamel
        } else {
            nameId
        }

    val signature
        get() = withWhereClause(
            "${trailingText(visibilityDecl)}${asyncKeyword}fn $rustFunctionName${genericParamSet?.asRust.emptyIfNull}$paramText$sigReturnType",
            genericParamSet
        )

    private val asyncKeyword = if (isAsync || isTokioTest) {
        "async "
    } else {
        ""
    }

    val asTraitFn
        get() = listOfNotNull(
            fnDoc,
            allAttrs.attrs.filter { attr ->
                // Filter out inline which is not appropriate in trait fn decls
                when (attr) {
                    is Attr.Word -> attr.id.snake != "inline"
                    is Attr.Value -> attr.id.snake != "inline"
                    else -> true
                } || body != null
            }.asOuterAttr,
            signature +
                    if (body != null) {
                        bracketText(indent(body.asRust)!!)
                    } else {
                        ";"
                    }
        ).joinNonEmpty()

    private val fnDoc = if (doc != null) {
        commentTriple(
            listOf(
                doc,
                joinNonEmpty(
                    params
                        .filter { it.nameId != "self" }
                        .joinToString("\n") { "  * ${it.nameId.bold} - ${it.doc}" },
                    if (returnType != null) {
                        "  * _return_ - $returnDoc"
                    } else {
                        ""
                    }
                )
            )
                .joinNonEmpty("\n\n")
        )
    } else {
        null
    }

    val testFunctionAttrs = testFnAttrs + listOfNotNull(
        attrTracingTest, if (hasTokioTest == true) {
            attrTokioTestFn
        } else {
            null
        },
        if (hasUnitTest == true) {
            attrTestFn
        } else {
            null
        }
    ).asAttrList

    fun asRust(codeBlockName: String) = listOfNotNull(
        fnDoc,
        allAttrs.asOuterAttr,
        "$signature {",
        localUses.map { it.asRust }.joinNonEmpty("\n"),
        consts.map { it.asRust }.joinNonEmpty("\n"),
        lets.map { it.asRust }.joinNonEmpty("\n"),
        indent(
            body?.asRust ?: emptyOpenDelimitedBlock(
                codeBlockName, emptyContents = emptyBlockContents ?: "todo!(\"Implement `$id`\")"
            )
        ),
        "}"
    )
        .joinNonEmpty()

    override val asRust: String get() = asRust("fn $blockName")

}