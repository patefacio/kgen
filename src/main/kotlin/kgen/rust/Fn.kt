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
    val visibility: Visibility = Visibility.Pub,
    val body: FnBody? = null,
    val isTest: Boolean = false,
    val hasUnitTest: Boolean? = null,
    val attrs: AttrList = AttrList(),
    val blockName: String = nameId,
    val emptyBlockContents: String? = null,
    val uses: Set<Use> = emptySet(),
    val testNameIds: List<String> = emptyList(),
    val panicTestNameIds: List<String> = emptyList(),
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
        attrs: AttrList = AttrList(),
        blockName: String = nameId,
        emptyBlockContents: String? = null,
        uses: Set<Use> = emptySet(),
        testNameIds: List<String> = emptyList(),
        panicTestNameIds: List<String> = emptyList()
    ) : this(
        nameId, doc, params.toList(), returnType, returnDoc, inlineDecl,
        genericParamSet, visibility, body, isTest, hasUnitTest, attrs,
        blockName, emptyBlockContents, uses, testNameIds, panicTestNameIds
    )

    private val allAttrs = listOfNotNull(
        attrs,
        if (inlineDecl != InlineDecl.None) {
            AttrList(inlineDecl.asRust())
        } else {
            null
        },
        if (isTest) {
            AttrList(attrTestFn)
        } else {
            null
        }
    ).reduce { acc, attrList -> acc + attrList }

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

    val visibilityDecl get() = if(!isTest) {
        visibility.asRust
    } else {
        ""
    }

    val signature
        get() = withWhereClause(
            "${trailingText(visibilityDecl)}fn $nameId${genericParamSet?.asRust.emptyIfNull}$paramText$sigReturnType",
            genericParamSet
        )

    val asTraitFn
        get() = listOfNotNull(
            fnDoc,
            allAttrs.attrs.filter { it != attrInline }.asOuterAttr,
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


    fun asRust(codeBlockName: String) = listOfNotNull(
        fnDoc,
        allAttrs.asOuterAttr,
        "$signature {",
        indent(body?.asRust ?: emptyOpenDelimitedBlock(codeBlockName, emptyContents = emptyBlockContents)),
        "}"
    )
        .joinNonEmpty()

    override val asRust: String get() = asRust("fn $blockName")

}