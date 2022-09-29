package kgen.rust

import kgen.*

data class Struct(
    val nameId: String,
    override val doc: String = missingDoc(nameId, "Struct"),
    val fields: List<Field> = emptyList(),
    val visibility: Visibility = Visibility.None,
    val uses: List<Use> = emptyList(),
    val genericParamSet: GenericParamSet = GenericParamSet()
) : Identifiable(nameId), Type, AsRust {

    val structName = id.capCamel

    private val header get() = "${trailingText(visibility.asRust)}struct ${structName}${genericParamSet.asRust} {"

    override val type: String
        get() = structName

    override val asRust: String
        get() = listOf(
            commentTriple(doc),
            header,
            if (fields.isEmpty()) {
                ""
            } else {
                indent(
                    fields.joinToString("\n") { it.asRust },
                ) ?: ""
            },
            "}"
        ).joinNonEmpty()

}