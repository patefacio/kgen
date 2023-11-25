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
 */
data class Field(
    val nameId: String,
    val doc: String = missingDoc(nameId, "Field"),
    val type: Type = RustString,
    val access: Access = Access.Pub,
    val attrs: AttrList = AttrList(),
    val excludeFromNew: Boolean = false,
    val defaultValue: String? = null,
    val customCtorInit: Boolean = false
) : Identifier(nameId), AsRust {

    val decl get() = "${trailingText(access.asRust)}$nameId: ${type.type}"

    private val tupleStructDecl get() = "${trailingText(access.asRust)}${type.type}"

    val inStructInitializer
        get() = when {
            defaultValue != null -> "$nameId: $defaultValue"
            excludeFromNew -> "$nameId: ${type.asRust}::default()"
            else -> nameId
        }

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
                        visibility = Visibility.Pub,
                        attrs = attrInline.asAttrList
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
                        visibility = Visibility.Pub,
                        attrs = attrInline.asAttrList
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
                        visibility = Visibility.Pub,
                        attrs = attrInline.asAttrList
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
