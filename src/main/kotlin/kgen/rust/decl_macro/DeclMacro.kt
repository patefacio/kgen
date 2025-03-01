package kgen.rust.decl_macro

import kgen.Identifier
import kgen.commentTriple
import kgen.joinNonEmpty
import kgen.missingDoc
import kgen.rust.*

data class DeclMacro(
    val nameId: String,
    val rules: List<Rule> = emptyList(),
    val doc: String = missingDoc(nameId, "Macro(Declarative)"),
    val attrs: AttrList = attrMacroExport.asAttrList,
    val visibility: Visibility = Visibility.Pub,
    val includeUse: Boolean = true
) : Identifier(nameId), AsRust {

    private val allAttrs = if (attrs.attrs.contains(attrMacroExport)) {
        attrs
    } else {
        attrs + attrMacroExport
    }

    val asRustName get() = nameId
    override val asRust: String
        get() = listOfNotNull(
            commentTriple(doc),
            allAttrs.asOuterAttr,
            "macro_rules! ${this.id.snakeCaseName} {",
            rules.joinToString(";\n") { it.asRust(nameId) },
            "}",
            if (includeUse) {
                Use(nameId, Visibility.PubCrate, allowUnusedImports = true).asRust
            } else {
                null
            }
        ).joinNonEmpty()

}
