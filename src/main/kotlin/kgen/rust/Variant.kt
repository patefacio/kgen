package kgen.rust

import kgen.*

sealed class Variant(val nameId: String, val doc: String, val attrs: AttrList = AttrList()) : AsRust,
    Identifier(nameId) {

    val docComment get() = commentTriple(doc)

    class Numeric(
        nameId: String,
        doc: String = missingDoc(nameId, "Enum.Numeric"),
        val value: Int,
        isDefault: Boolean = false,
        attrs: AttrList = AttrList()
    ) : Variant(
        nameId, doc, attrs = attrs + if (isDefault) {
            Attr.Word("default").asAttrList
        } else {
            AttrList()
        }
    ) {
        override val asRust: String
            get() = listOfNotNull(
                attrs.asOuterAttr,
                docComment,
                "${id.capCamel} = $value"
            ).joinNonEmpty()
    }

    class UnitStruct(
        nameId: String,
        doc: String = missingDoc(nameId, "Enum.UnitStruct"),
        isDefault: Boolean = false,
        attrs: AttrList = AttrList()
    ) :
        Variant(
            nameId, doc, attrs = attrs + if (isDefault) {
                Attr.Word("default").asAttrList
            } else {
                AttrList()
            }
        ) {
        override val asRust: String
            get() = listOfNotNull(
                attrs.asOuterAttr,
                docComment,
                id.capCamel
            ).joinNonEmpty()
    }

    class TupleStruct(
        nameId: String,
        doc: String = missingDoc(nameId, "Enum.TupleStruct"),
        val types: List<Type>,
        isDefault: Boolean = false,
        attrs: AttrList = AttrList()
    ) :
        Variant(
            nameId, doc, attrs = attrs + if (isDefault) {
                Attr.Word("default").asAttrList
            } else {
                AttrList()
            }
        ) {

        constructor(
            nameId: String,
            doc: String = missingDoc(nameId, "Enum.TupleStruct"),
            vararg types: Type,
            isDefault: Boolean = false,
            attrs: AttrList = AttrList()
        ) : this(
            nameId,
            doc,
            types.toList(),
            isDefault,
            attrs
        )

        override val asRust: String
            get() = listOfNotNull(
                attrs.asOuterAttr,
                docComment,
                "${id.capCamel}(${types.joinToString { it.asRust }})"
            )
                .joinNonEmpty()
    }

    class Struct(
        nameId: String,
        doc: String = missingDoc(nameId, "Enum.Struct"),
        val fields: List<Field>,
        attrs: AttrList = AttrList()
    ) :
        Variant(nameId, doc, attrs) {

        constructor(
            nameId: String,
            doc: String = missingDoc(nameId, "Enum.Struct"),
            vararg fields: Field,
            attrs: AttrList = AttrList()
        ) : this(
            nameId,
            doc,
            fields.toList(),
            attrs
        )

        override val asRust: String
            get() = listOfNotNull(
                attrs.asOuterAttr,
                docComment,
                "${id.capCamel} {",
                indent(fields.joinToString(",\n") {
                    it.copy(access = Access.None).asRust
                }),
                "}"
            )
                .joinNonEmpty()
    }
}