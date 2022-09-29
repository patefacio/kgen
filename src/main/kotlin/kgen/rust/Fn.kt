package kgen.rust

import kgen.*

enum class InlineDecl {
    Inline,
    InlineAlways,
    InlineNever,
    None;

    fun asRust() = when (this) {
        Inline -> Attr.Word("inline")
        InlineAlways -> Attr.Value("inline", "always")
        InlineNever -> Attr.Value("inline", "never")
        None -> throw RuntimeException("No AsRust for Inline.None")
    }
}

data class Fn(
    val nameId: String,
    val doc: String = missingDoc(nameId),
    val params: List<FnParam> = emptyList(),
    val returnType: Type? = null,
    val returnDoc: String? = null,
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
        doc: String = missingDoc(nameId),
        vararg params: FnParam,
        returnType: Type? = null,
        returnDoc: String? = null,
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

    private fun returnDoc() = if (returnType == null) {
        null
    } else {
        returnDoc ?: "TODO: Document Return Type $returnType"
    }

    companion object {
        fun missingDoc(id: String) = "TODO: Document $id"
    }

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

    private val signature
        get() = "fn $nameId$paramText$sigReturnType"

    private val fnDoc = listOf(
        doc,
        params
            .filter { it != self && it != refSelf && it != refMutSelf }
            .joinToString("\n") { "  * ${it.nameId} - ${it.doc}" },
        if (returnDoc != null) {
            "  returns - ${returnDoc}"
        } else {
            ""
        }
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