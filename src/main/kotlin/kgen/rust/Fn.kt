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
    private val signature
        get() = "fn $nameId$paramText"

    private val fnDoc = listOf(
        doc,
        params.joinToString("\n") { "  * ${it.nameId} - ${it.doc}" },
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