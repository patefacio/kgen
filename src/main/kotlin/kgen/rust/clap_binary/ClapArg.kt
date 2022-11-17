package kgen.rust.clap_binary

import kgen.Identifier
import kgen.doubleQuote
import kgen.rust.*

data class ClapArg(
    val nameId: String,
    val doc: String,
    val type: Type = RustString,
    val includeLong: Boolean = true,
    val includeShort: Boolean = true,
    val isOptional: Boolean = false,
    val isMultiple: Boolean = false,
    val defaultValue: Any? = null
) : Identifier(nameId) {

    val attrList
        get() = AttrList(
            Attr.Dict(
                "arg", listOfNotNull(
                    if (includeLong) {
                        "long" to null
                    } else {
                        null
                    },
                    if (includeShort) {
                        "short" to null
                    } else {
                        null
                    },
                    when(defaultValue) {
                        null -> null
                        else -> "default_value" to defaultValue.toString()
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