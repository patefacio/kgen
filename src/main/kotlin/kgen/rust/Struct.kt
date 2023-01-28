package kgen.rust

import kgen.*

/**
 *
 *  @property implementedTraits - Traits to include an implementation for. Often
 *  structs are modeled and then in the module a trait is implemented for the
 *  struct with `TraitImpl(someTrait, "SomeStruct".type, ...)`. This is a
 *  alternative convenience - simply add a trait like `defaultTrait` and
 *  a stubbed impl will be included.
 */
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
    val implementedTraits: List<Trait> = emptyList(),
    val staticInits: List<StaticInit> = emptyList(),
    val includeNew: Boolean = false,
    val includeCustomNew: Boolean = false,
    val inlineNew: Boolean = false
) : Identifier(nameId), Type, AsRust {

    val structName = id.capCamel

    val structNameGeneric get() = "${structName}${genericParamSet.asRust}"

    val structNameGenericWithoutDefaults get() = "${structName}${genericParamSet.withoutDefaults.asRust}"

    val allUses get() = uses + (typeImpl?.allUses ?: emptySet())

    private fun newFnFromFields(fields: List<Field>): Fn {
        val returnType = structNameGenericWithoutDefaults.asType
        val includedFields = fields.filter { !it.excludeFromNew }
        fun wrapNewBody(body: String) = if (asTupleStruct) {
            "(\n$body\n)"
        } else {
            "{\n$body\n}"
        }

        // If all fields are included, generate a forwarding body
        val body = if (!includeCustomNew) {
            FnBody(
                listOf(
                    asRustName,
                    wrapNewBody(indent(fields.joinToString(",\n") { it.inStructInitializer })!!),
                ).joinToString("\n")
            )
        } else {
            null
        }

        return Fn(
            "new",
            "Create new instance of $asRustName",
            includedFields.filter { it.defaultValue == null }.map { it.asFnParam },
            returnDoc = "The new instance",
            returnType = returnType,
            body = body,
            hasUnitTest = false,
            attrs = if (inlineNew) {
                attrInline.asAttrList
            } else {
                AttrList()
            }
        )
    }

    val traitImpls
        get() = implementedTraits.map { trait ->
            TraitImpl(structNameGeneric.asType, trait, genericParamSet = genericParamSet)
        }

    val augmentedTypeImpl
        get() = if (includeNew || includeCustomNew) {
            val newFn = newFnFromFields(fields)
            typeImpl?.copy(
                functions = typeImpl.functions + newFn,
                genericParamSet = genericParamSet.withoutDefaults
            ) ?: TypeImpl(
                newFn.returnType!!,
                newFn,
                genericParamSet = genericParamSet.withoutDefaults
            )
        } else {
            typeImpl
        }

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
        implementedTraits: List<Trait> = emptyList(),
        staticInits: List<StaticInit> = emptyList(),
        includeNew: Boolean = false,
        includeCustomNew: Boolean = false,
        inlineNew: Boolean = false
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
        implementedTraits,
        staticInits,
        includeNew,
        includeCustomNew,
        inlineNew
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
                "${trailingText(visibility.asRust)}struct $structNameGeneric",
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

