package kgen.rust

import kgen.*

data class Fn(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Fn"),
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
    val blockName: String = nameId
) : Identifiable(nameId), AsRust {


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
        blockName: String = nameId
    ) : this(
        nameId, doc, params.toList(), returnType, returnDoc, inlineDecl,
        genericParamSet, visibility, body, isTest, hasUnitTest, attrs,
        blockName
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

    private fun withBoundsDecl(text: String): String {
        val whereClause = genericParamSet?.whereClause
        return if (whereClause == null) {
            text
        } else {
            "$text\nwhere\n${indent(whereClause)}"
        }
    }

    val signature
        get() = withBoundsDecl("fn $nameId${genericParamSet?.asRust.emptyIfNull}$paramText$sigReturnType")

    val asTraitFn
        get() = listOfNotNull(
            commentTriple(fnDoc),
            allAttrs.asRust,
            signature +
                    if (body != null) {
                        bracketText(indent(body.asRust)!!)
                    } else {
                        ";"
                    }
        ).joinNonEmpty()

    private val fnDoc = listOf(
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


    override val asRust: String
        get() = listOfNotNull(
            commentTriple(fnDoc),
            allAttrs.asRust,
            "$signature {",
            indent(body?.asRust ?: emptyBlock(blockName)),
            "}"
        )
            .joinNonEmpty()
}