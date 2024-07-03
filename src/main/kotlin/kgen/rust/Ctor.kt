package kgen.rust

import kgen.emptyOpenDelimitedBlock
import kgen.indent
import kgen.joinNonEmpty

/** Models a constructor, which in rust is an associated function that
 * creates an instance of the implemented type. Provides a mechanism for
 * adding multiple constructors for a type. The naming of the constructor
 * follows the rust convention of using `new`.
 *
 * @property name The name of the ctor - if null is `new`
 * @property included The field names to include as arguments - do not use with `excluded`
 * @property excluded The field names to exclude as arguments - do not use with `included`
 * @property additionalFnParams List of params not represented by fields
 * @property isInline Adds the inline attribute
 * @property attrs Attributes for the function, `inline` being one potential attr
 * @property excludeInitializer If true will not finish ctor with initialized value. Useful
 *    for forwarding ctor calls
 * @property hasUnitTest Include a unit test
 * @property useLetAssignments Changes style to use let assignments for all fields with
 *    default values. The benefit is the custom initializers can then use those values.
 */
data class Ctor(
    val name: String = "new",
    val doc: String? = "Initializer",
    val included: Set<String> = emptySet(),
    val includedFilter: ((Field) -> Boolean)? = null,
    val excluded: Set<String> = emptySet(),
    val excludedFilter: ((Field) -> Boolean)? = null,
    val additionalFnParams: List<FnParam> = emptyList(),
    val isInline: Boolean = false,
    val attrs: AttrList = AttrList(),
    val excludeInitializer: Boolean = false,
    val hasUnitTest: Boolean = false,
    val useLetAssignments: Boolean = false,
    val isAsync: Boolean = false
) {

    private fun includedFields(fields: List<Field>) = when {
        included.isNotEmpty() -> {
            fields.filter { it.id.snakeCaseName in included }
        }

        includedFilter != null -> fields.filter(includedFilter)

        excluded.isNotEmpty() -> {
            fields.filter { it.id.snakeCaseName !in excluded }
        }

        excludedFilter != null -> fields.filterNot(excludedFilter)

        else -> fields
    }.filterNot { it.customCtorInit }

    /** The [Ctor] transformed to a rust [Fn] */
    fun asFn(fields: List<Field>, typeName: String, isTupleStruct: Boolean = false): Fn {
        val hasCustomInitFields =
            fields.any { it.customCtorInit } || excluded.isNotEmpty() || excludedFilter != null ||
                    included.isNotEmpty() || includedFilter != null
        val customBlock = if (hasCustomInitFields) {
            emptyOpenDelimitedBlock("$typeName::$name initialization")
        } else {
            null
        }
        val includedFields = includedFields(fields)
        val fnParams = includedFields.map { it.asFnParam } + additionalFnParams

        fun wrapNewBody(body: String) = if (isTupleStruct) {
            "(\n$body\n)"
        } else {
            "{\n$body\n}"
        }

        // If there are custom initialized fields, change the approach to initialize
        // defaulted fields via let assignments. This way those can be used for custom
        // initializations.
        val letAssignments = if (useLetAssignments) {
            fields.filter { it.defaultValue != null }
                .joinToString("\n") { "let ${it.id.snakeCaseName} = ${it.defaultValue};" }
        } else {
            null
        }

        val functionBody = listOfNotNull(
            letAssignments,
            customBlock,
            "Self ${
                wrapNewBody(
                    indent(fields.joinToString(",\n") {
                        // If the field is included as a parameter, pass it through
                        if (includedFields.contains(it) || useLetAssignments) {
                            it.id.snakeCaseName
                        } else {
                            it.inStructInitializer
                        }
                    })!!
                )
            }"
        ).joinNonEmpty()


        return Fn(
            name,
            doc,
            params = fnParams,
            returnDoc = "The constructed instance",
            returnType = "Self".asType,
            attrs = attrs,
            body = if (!excludeInitializer) {
                FnBody(
                    body = functionBody
                )
            } else {
                null
            },
            hasUnitTest = hasUnitTest,
            isAsync = isAsync
        )
    }
}