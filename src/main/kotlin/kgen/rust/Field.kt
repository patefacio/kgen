package kgen.rust

import kgen.*

/**
 * Returns a struct field.
 * @property id - Identifies the field within a struct
 * @property doc - Documentation for the field
 * @property type - Type of the field
 * @property access - Resolves to visibility and accessor functions
 * @property excludeFromNew - If in struct specifying `includeNew` this field
 *   will be skipped. Convenient way to leave
 * @property defaultValue Associate a default value for a field. If a struct
 *   is auto-generating a ctor, this can be used to initialize it.
 * @property customCtorInit When creating ctors, if this is set the field is
 * not passed in but custom initialized
 * @property testAccessOnly If set and if access not `pub` the accessors are only available
 * to test
 * @property escapeName If set the name is should be escaped (eg for a name that is also a rust keyword)
 */
data class Field(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Field"),
    val type: Type = RustString,
    val access: Access = Access.Pub,
    val attrs: AttrList = AttrList(),
    val excludeFromNew: Boolean = false,
    val defaultValue: String? = null,
    val customCtorInit: Boolean = false,
    val testAccessOnly: Boolean = false,
    val escapeName: Boolean = false,
) : Identifier(nameId), AsRust {

    /** The field namd as used in declaration, escaped if necessary */
    private val declName get() = if (escapeName) "r#$escapeName" else nameId

    /** The field declaration */
    val decl get() = "${trailingText(access.asRust)}$declName: ${type.type}"

    private val tupleStructDecl get() = "${trailingText(access.asRust)}${type.type}"

    val inStructInitializer
        get() = when {
            defaultValue != null -> "$nameId: $defaultValue"
            excludeFromNew -> "$nameId: ${type.asRust}::default()"
            else -> nameId
        }

    private val testConfig get() = if (testAccessOnly) attrCfgTest else null

    private val accessorAttrs get() = listOfNotNull(attrInline, testConfig).asAttrList

    val accessors
        get() = listOfNotNull(
            when {
                access.requiresReader -> {
                    Fn(
                        "get_${id.snake}",
                        null,
                        refSelf,
                        body = FnBody("self.${id.snake}"),
                        returnDoc = "The value.",
                        returnType = type,
                        attrs = accessorAttrs,
                    )
                }

                access.requiresRefReader -> {
                    Fn(
                        "get_${id.snake}",
                        null,
                        refSelf,
                        body = FnBody("& self.${id.snake}"),
                        returnDoc = "The value.",
                        returnType = "& ${type.asRust}".asType,
                        attrs = accessorAttrs,
                    )
                }

                access.requiresReaderCloned -> {
                    Fn(
                        "get_${id.snake}",
                        null,
                        refSelf,
                        body = FnBody("self.${id.snake}.clone()"),
                        returnDoc = "The value.",
                        returnType = "${type.asRust}".asType,
                        attrs = accessorAttrs,
                    )
                }

                else -> null
            }
        ) + listOfNotNull(
            when {
                access.requiresRefWriter -> {
                    Fn(
                        "get_${id.snake}_mut",
                        null,
                        refMutSelf,
                        body = FnBody("& mut self.${id.snake}"),
                        returnType = "&mut ${type.asRust}".asType,
                        returnDoc = "Mutable reference to data",
                        attrs = accessorAttrs,
                    )
                }

                else -> null
            }
        )


    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            attrs.asOuterAttr,
            decl
        ).joinNonEmpty("\n")

    val asTupleStructField
        get() = listOf(
            commentTriple(doc),
            attrs.asOuterAttr,
            tupleStructDecl
        ).joinNonEmpty("\n")

    val asFnParamRef get() = FnParam(nameId, "& ${type.asRust}", doc)
    val asFnParam get() = FnParam(nameId, type.asRust, doc)
}
