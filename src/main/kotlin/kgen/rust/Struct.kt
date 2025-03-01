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

/**
 * Represents a Rust structure, including its fields, attributes, visibility, and related implementations.
 *
 * This class provides utilities for generating the Rust representation of a structure, as well as
 * helper methods for trait implementations, constructors, and accessors.
 *
 * @property nameId The unique identifier (name) for the structure.
 * @property doc Documentation string associated with the structure. Defaults to a generated placeholder if not provided.
 * @property fields A list of fields contained in the structure. Defaults to an empty list.
 * @property visibility The visibility level of the structure (e.g., public or private). Defaults to `Pub`.
 * @property uses A set of `use` statements required by the structure. Defaults to an empty set.
 * @property genericParamSet Generic parameters associated with the structure. Defaults to an empty parameter set.
 * @property attrs A list of attributes applied to the structure. Defaults to an empty `AttrList`.
 * @property asTupleStruct Indicates if the structure is represented as a tuple struct. Defaults to `false`.
 * @property typeImpl A type implementation associated with the structure, if any. Defaults to `null`.
 * @property traitImpls A list of explicit trait implementations for the structure. Defaults to an empty list.
 * @property implementedTraits A list of traits that this structure implements. Defaults to an empty list.
 * Often structs are modeled and then in the module a trait is implemented for the struct with
 * `TraitImpl(someTrait, "SomeStruct".type, ...)`. This is an alternative convenience - simply add a
 * trait like `defaultTrait` and a stubbed impl will be included.
 * @property staticInits A list of static initializers defined within the structure. Defaults to an empty list.
 * @property lazies A list of lazily initialized properties associated with the structure. Defaults to an empty list.
 * @property includeNew Indicates whether a default constructor (`new`) should be generated. Defaults to `false`.
 * @property includeCustomNew Indicates whether a custom `new` constructor should be generated. Defaults to `false`.
 * @property inlineNew Indicates whether the `new` constructor should be inlined. Defaults to `false`.
 * @property additionalNewParams A list of additional parameters for the `new` constructor. Defaults to an empty list.
 * @property deriveBuilder Indicates whether a builder pattern should be derived for the structure. Defaults to `false`.
 * @property ctors A list of additional constructors defined for the structure. Defaults to an empty list.
 * @property consts A list of constants defined within the structure. Defaults to an empty list.
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
    val traitImpls: List<TraitImpl> = emptyList(),
    val implementedTraits: List<Trait> = emptyList(),
    val staticInits: List<StaticInit> = emptyList(),
    val lazies: List<Lazy> = emptyList(),
    val includeNew: Boolean = false,
    val includeCustomNew: Boolean = false,
    val inlineNew: Boolean = false,
    val additionalNewParams: List<FnParam> = emptyList(),
    val deriveBuilder: Boolean = false,
    val ctors: List<Ctor> = emptyList(),
    val consts: List<Const> = emptyList(),
) : Identifier(nameId), Type, AsRust {

    val structName = id.capCamel

    val structNameGeneric get() = "${structName}${genericParamSet.asRust}"

    val structNameGenericWithoutDefaults get() = "${structName}${genericParamSet.withoutDefaults.asRust}"

    val allUses
        get() = uses + (typeImpl?.allUses ?: emptySet()) + if (deriveBuilder) {
            listOf("derive_builder::Builder").asUses
        } else {
            emptySet()
        }

    val allAttrs
        get() = attrs + if (deriveBuilder) {
            attrDeriveBuilder
        } else {
            AttrList()
        }

    private fun newFnFromFields(fields: List<Field>, additionalNewParams: List<FnParam>): Fn {
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
            includedFields.filter { it.defaultValue == null }.map { it.asFnParam } + additionalNewParams,
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

    val allTraitImpls
        get() = traitImpls + implementedTraits.map { trait ->
            TraitImpl(structNameGeneric.asType, trait, genericParamSet = genericParamSet)
        }

    private val allCtorFns = ctors.map { it.asFn(fields, structName) } + listOfNotNull(
        when {
            includeNew || includeCustomNew -> {
                newFnFromFields(fields, additionalNewParams)
            }

            else -> null
        }
    )

    private val hasCtor = includeNew || includeCustomNew || allCtorFns.isNotEmpty()

    val augmentedTypeImpl
        get() = if (hasCtor) {
            typeImpl?.copy(
                functions = typeImpl.functions + allCtorFns,
                genericParamSet = genericParamSet.withoutDefaults
            ) ?: TypeImpl(
                structNameGenericWithoutDefaults.asType,
                allCtorFns,
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
        traitImpls: List<TraitImpl> = emptyList(),
        implementedTraits: List<Trait> = emptyList(),
        staticInits: List<StaticInit> = emptyList(),
        lazies: List<Lazy> = emptyList(),
        includeNew: Boolean = false,
        includeCustomNew: Boolean = false,
        inlineNew: Boolean = false,
        additionalNewParams: List<FnParam> = emptyList(),
        deriveBuilder: Boolean = false,
        ctors: List<Ctor> = emptyList(),
        consts: List<Const> = emptyList(),
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
        traitImpls,
        implementedTraits,
        staticInits,
        lazies,
        includeNew,
        includeCustomNew,
        inlineNew,
        additionalNewParams,
        deriveBuilder,
        ctors,
        consts
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
            allAttrs.asOuterAttr,
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

    val constsTypeImpl
        get() = if (consts.isNotEmpty()) {
            TypeImpl(asRustName.asType, consts = consts)
        } else {
            null
        }

}

val Id.asStructName get() = capCamel
val String.asStructName get() = asId.asStructName

