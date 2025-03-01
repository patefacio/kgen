package kgen.rust.clap_binary

import kgen.Identifier
import kgen.rust.*

/**
 * Represents a command-line argument for a Clap-based Rust application.
 *
 * This class generates the corresponding argument definitions and attributes for use with the `clap` library,
 * enabling flexible configuration of command-line arguments.
 *
 * @property nameId The unique identifier (name) of the argument.
 * @property doc Documentation string describing the purpose of the argument.
 * @property type The Rust type of the argument. Defaults to `RustString`.
 * @property includeLong Indicates whether a `--long` form of the argument should be included. Defaults to `true`.
 * @property longName The custom long name for the argument. If `null`, the `nameId` is used by default.
 * @property includeShort Indicates whether a `-s` (short) form of the argument should be included. Defaults to `true`.
 * @property shortName The custom short name for the argument. If `null`, the first character of `nameId` is used.
 * @property isOptional Indicates whether the argument is optional (`Option<T>`). Defaults to `false`.
 * @property isMultiple Indicates whether the argument can accept multiple values (`Vec<T>`). Defaults to `false`.
 * @property defaultValue The default value for the argument, provided as a string. Defaults to `null`.
 * @property defaultLiteralValue The default value for the argument, represented as a Rust literal. Defaults to `null`.
 * @property isEnum Indicates whether the argument represents an enumerated type. Defaults to `false`.
 */
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

    /**
     * Generates the list of attributes for the `clap` argument.
     *
     * Includes options such as `--long`, `-s` (short), default values, and the `value_enum` flag if the argument
     * is an enumerated type.
     *
     * @return An [AttrList] containing the attributes for the argument.
     */
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

    /**
     * Generates the corresponding Rust field for the argument.
     *
     * The field type is determined by the configuration of `isOptional` and `isMultiple`:
     * - If `isOptional = true`: The type is wrapped in `Option<T>`.
     * - If `isMultiple = true`: The type is wrapped in `Vec<T>`.
     * - Otherwise, the raw type is used.
     *
     * @return A [Field] representing the argument in the generated Rust struct.
     */
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

/**
 * Represents the standard log-level argument for a CLI application.
 *
 * This argument allows the user to specify the desired log level (e.g., debug, info, warn), and
 * it maps to a `LogLevel` enum in Rust. The default value is set to `LogLevel::Warn`.
 *
 * @return A [ClapArg] preconfigured for log-level selection.
 */
val standardLogLevelArg = ClapArg(
    "log_level",
    "Log-level for the run.",
    type = "LogLevel".asType,
    defaultLiteralValue = "LogLevel::Warn",
    longName = "ll",
    isEnum = true
)
