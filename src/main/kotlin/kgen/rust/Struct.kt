package kgen.rust

import kgen.*

data class Struct(
    val nameId: String,
    override val doc: String = missingDoc(nameId, "Struct"),
    val fields: List<Field> = emptyList(),
    val visibility: Visibility = Visibility.Pub,
    val uses: Set<Use> = emptySet(),
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val attrs: AttrList = AttrList(),
    val asTupleStruct: Boolean = false,
    val typeImpl: TypeImpl? = null,
    val staticInits: List<StaticInit> = emptyList()
) : Identifier(nameId), Type, AsRust {

    val structName = id.capCamel

    val allUses = uses + (typeImpl?.allUses ?: emptySet())
    val accessors
        get() = fields.fold(mutableListOf<Fn>()) { acc, field ->
            acc.addAll(field.accessors)
            acc
        }.toList()

    override val asRustName: String
        get() = structName

    val asBorrowChecked: String
        get() = if (genericParamSet.lifetimes.isNotEmpty()) {
            "${asRustName}${genericParamSet.lifetimes.asRust}"
        } else {
            asRustName
        }

    constructor(
        nameId: String,
        doc: String,
        vararg fields: Field,
        visibility: Visibility = Visibility.Pub,
        uses: Set<Use> = emptySet(),
        genericParamSet: GenericParamSet = GenericParamSet(),
        attrs: AttrList = AttrList(),
        asTupleStruct: Boolean = false,
        typeImpl: TypeImpl? = null,
        staticInits: List<StaticInit> = emptyList()
    ) : this(
        nameId,
        doc,
        fields.toList(),
        visibility,
        uses,
        genericParamSet,
        attrs,
        asTupleStruct,
        typeImpl,
        staticInits
    )

    private val openStruct = if (asTupleStruct) {
        "("
    } else {
        " {"
    }
    private val closeStruct = if (asTupleStruct) {
        ")"
    } else {
        "}"
    }

    private val header
        get() =
            withWhereClause(
                "${trailingText(visibility.asRust)}struct ${structName}${genericParamSet.asRust}",
                genericParamSet
            ) + openStruct

    override val type: String
        get() = structName

    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            attrs.asOuterAttr,
            header,
            if (fields.isEmpty()) {
                ""
            } else {
                indent(
                    fields.joinToString(",\n") {
                        if (asTupleStruct) {
                            it.asTupleStructField
                        } else {
                            it.asRust
                        }
                    },
                ) ?: ""
            },
            closeStruct,
            if (asTupleStruct) {
                ";"
            } else {
                ""
            }
        ).joinNonEmpty()

}

val Id.asStructName get() = capCamel
val String.asStructName get() = asId.asStructName