package kgen.rust.clap_binary

import kgen.Identifier
import kgen.charQuote
import kgen.doubleQuote
import kgen.rust.*

data class ClapArg(
    val nameId: String,
    val doc: String,
    val type: Type = RustString,
    val includeLong: Boolean = true,
    val longName: String? = null,
    val includeShort: Boolean = true,
    val shortName: String? = null,
    val isOptional: Boolean = false,
    val isMultiple: Boolean = false,
    val defaultValue: String? = null,
    val defaultLiteralValue: String? = null,
    val isEnum: Boolean = false
) : Identifier(nameId) {

    val attrList
        get() = AttrList(
            Attr.Dict(
                "arg", listOfNotNull(
                    if (includeLong) {
                        "long" to longName
                    } else {
                        null
                    },
                    if (includeShort) {
                        "short" to if (shortName?.isNotEmpty() == true) {
                            shortName.first()
                        } else {
                            null
                        }
                    } else {
                        null
                    },
                    if (isEnum) {
                        "value_enum" to null
                    } else {
                        null
                    },
                    when (defaultValue) {
                        null -> null
                        else -> "default_value" to defaultValue
                    },
                    when (defaultLiteralValue) {
                        null -> null
                        else -> "default_value_t" to DictValue.LiteralValue(defaultLiteralValue)
                    }
                )
            )
        )


    val field
        get() = Field(
            nameId,
            doc,
            type = when {
                isOptional -> "Option<${type.asRust}>".asType
                isMultiple -> "Vec<${type.asRust}>".asType
                else -> type
            },
            attrs = attrList
        )


}