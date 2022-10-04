package kgen.rust

import kgen.indent
import kgen.joinNonEmpty
import kgen.trailingText

data class TraitImpl(
    val type: Type,
    val trait: Trait,
    val doc: String = missingDoc(trait.nameId, "TraitImpl"),
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val genericArgSet: GenericArgSet = GenericArgSet(),
    val associatedTypeAssignments: List<String> = emptyList(),
    val bodies: Map<String, String> = emptyMap()
) : AsRust {

    override val asRust: String
        get() = listOf(
            withWhereClause(
                "impl ${trailingText(genericParamSet.asRust)}${trait.asRustName} for ${type.asRust}",
                genericParamSet
            ) + " {",
            indent(
                trailingText(
                    associatedTypeAssignments.map { "type $it;" }.joinNonEmpty("\n\n")
                )
            ),
            indent(
                trait.functions.filter { it.body == null }.joinToString("\n\n") {
                    if (bodies.contains(it.nameId)) {
                        it.copy(body = FnBody(bodies.getValue(it.nameId))).asRust
                    } else {
                        it.asRust
                    }
                }),
            "}"
        ).joinToString("\n")

}