package kgen.rust

import kgen.commentTriple
import kgen.indent
import kgen.trailingSpace

data class Trait(
    val nameId: String,
    val doc: String = "TODO: Document Trait($nameId)",
    val functions: List<Fn> = emptyList(),
    val genericParamSet: GenericParamSet = GenericParamSet(),
    val visibility: Visibility = Visibility.None,
    val associatedTypes: List<AssociatedType> = emptyList(),
    val superTraits: List<Trait> = emptyList(),
    val uses: List<Use> = emptyList()
) : Identifiable(nameId), AsRust {

    val traitScopedFunctions = functions.map {
        it.copy(blockName = "trait fn ${id.capCamel}::${it.blockName}")
    }

    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            trailingSpace("trait ${id.capCamel}${genericParamSet.asRust} {", "\n"),
            indent(traitScopedFunctions.joinToString("\n\n") { it.asRust }),
            "}"
        ).joinToString("\n")

}

