package kgen.rust

import kgen.*

data class Fn(
    val nameId: String,
    val doc: String? = missingDoc(nameId, "Fn"),
    val params: List<FnParam> = emptyList(),
    val returnType: Type? = null,
    val returnDoc: String? = missingDoc(nameId, "FnReturn"),
    val inlineDecl: InlineDecl = InlineDecl.None,
    val genericParamSet: GenericParamSet? = null,
    val visibility: Visibility = Visibility.None,
    val body: FnBody? = null,
    val isTest: Boolean = false,
    val hasUnitTest: Boolean = false,
    val attrs: AttrList = AttrList(),
    val blockName: String = nameId,
    val uses: Set<Use> = emptySet()
) : Identifier(nameId), AsRust {


    constructor(
        nameId: String,
        doc: String = missingDoc(nameId, "Fn"),
        vararg params: FnParam,
        returnType: Type? = null,
        returnDoc: String? = missingDoc(nameId, "FnReturn"),
        inlineDecl: InlineDecl = InlineDecl.None,
        genericParamSet: GenericParamSet? = null,
        visibility: Visibility = Visibility.None,
        body: FnBody? = null,
        isTest: Boolean = false,
        hasUnitTest: Boolean = false,
        attrs: AttrList = AttrList(),
        blockName: String = nameId,
        uses: Set<Use> = emptySet()
    ) : this(
        nameId, doc, params.toList(), returnType, returnDoc, inlineDecl,
        genericParamSet, visibility, body, isTest, hasUnitTest, attrs,
        blockName, uses
    )

    private val allAttrs = if (inlineDecl == InlineDecl.None) {
        attrs
    } else {
        AttrList(attrs.attrs + inlineDecl.asRust())
    }

    private var paramText = if (params.isEmpty()) {
        "()"
    } else {
        listOf(
            "(",
            indent(params.joinToString(",\n") { it.asRust }),
            ")"
        ).joinToString("\n")
    }

    private val sigReturnType
        get() = if (returnType != null) {
            " -> ${returnType.asRust}"
        } else {
            ""
        }

    val signature
        get() = withWhereClause(
            "${trailingText(visibility.asRust)}fn $nameId${genericParamSet?.asRust.emptyIfNull}$paramText$sigReturnType",
            genericParamSet
        )

    val asTraitFn
        get() = listOfNotNull(
            fnDoc,
            allAttrs.asRust,
            signature +
                    if (body != null) {
                        bracketText(indent(body.asRust)!!)
                    } else {
                        ";"
                    }
        ).joinNonEmpty()

    private val fnDoc = if (doc != null) {
        commentTriple(listOf(
            doc,
            joinNonEmpty(
                params
                    .filter { it != self && it != refSelf && it != refMutSelf }
                    .joinToString("\n") { "  * ${it.nameId} - ${it.doc}" },
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


    fun asRust(codeBlockName: String) = listOfNotNull(
        fnDoc,
        allAttrs.asRust,
        "$signature {",
        indent(body?.asRust ?: emptyBlock(codeBlockName)),
        "}"
    )
        .joinNonEmpty()

    override val asRust: String get() = asRust("fn $blockName")

}