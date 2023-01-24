package kgen.rust

import kgen.joinNonEmpty
import kgen.nullIfEmpty

data class GenericParamSet(
    val lifetimes: List<Lifetime> = emptyList(),
    val typeParams: List<TypeParam> = emptyList()
) : AsRust {

    constructor(vararg typeParams: TypeParam, lifetimes: List<String> = emptyList()) :
            this(lifetimes.map { Lifetime(it) }, typeParams.toList())

    constructor(vararg typeParams: String, lifetimes: List<Lifetime> = emptyList()) :
            this(lifetimes, typeParams.map { TypeParam(it) }.toList())

    val isEmpty get() = lifetimes.isEmpty() && typeParams.isEmpty()

    override val asRust: String
        get() = if (isEmpty) {
            ""
        } else {
            "<${
                (lifetimes.map { it.asRust } + typeParams.map {
                    it.asRust
                }).joinToString(", ")
            }>"
        }

    val whereClause
        get() = typeParams.mapNotNull { it.boundsDecl }.joinNonEmpty(", ").nullIfEmpty

    fun genericTypeOf(t: Type) = "${t.asRust}${asRust}"

}

val tickA = GenericParamSet(lifetimes = listOf("a"))
val String.asTickAType get() = "${this}<'a>".asType
