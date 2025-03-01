package kgen.rust

import kgen.joinNonEmpty
import kgen.nullIfEmpty

/** Models support for specifying generic parameters for rust items supporting
 * generics - `Struct`, `Enum`, `fn`, `TypeAlias`,...
 *
 * @property lifetimes Any lifetimes
 * @property typeParams The type parameters
 */
data class GenericParamSet(
    val lifetimes: List<Lifetime> = emptyList(),
    val typeParams: List<TypeParam> = emptyList()
) : AsRust {

    constructor(vararg typeParams: TypeParam, lifetimes: List<String> = emptyList()) :
            this(lifetimes.map { Lifetime(it) }, typeParams.toList())

    constructor(vararg typeParams: String, lifetimes: List<Lifetime> = emptyList()) :
            this(lifetimes, typeParams.map { TypeParam(it) }.toList())

    /** True if the parameter set has no lifetimes or type parameters */
    val isEmpty get() = lifetimes.isEmpty() && typeParams.isEmpty()

    override val asRust: String
        get() = if (isEmpty) {
            ""
        } else {
            "<${
                (lifetimes.map { it.asRust } + typeParams.map {
                    it.asRustGenericDecl
                }).joinToString(", ")
            }>"
        }

    val whereClause
        get() = typeParams.mapNotNull { it.boundsDecl }.joinNonEmpty(", ").nullIfEmpty

    fun genericTypeOf(t: Type) = "${t.asRust}${asRust}".asType
    fun genericTypeOf(typeName: String) = "${typeName.asType.asRust}${asRust}".asType

    val withoutDefaults get() = GenericParamSet(lifetimes, typeParams.map { it.copy(default = null) })

}

val tickA = GenericParamSet(lifetimes = listOf("a"))
val String.asTickAType get() = "${this}<'a>".asType
