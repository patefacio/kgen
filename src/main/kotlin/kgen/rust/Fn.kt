package kgen.rust

import kgen.Id
import kgen.id

data class Fn(
    val nameId: String,
    val doc: String = missingDoc(nameId),
    val params: List<FnParam> = emptyList(),
    val returnType: Type? = null,
    val returnDoc: String? = null,
    val isInline: Boolean = false,
    val genericParamSet: GenericParamSet? = null,
    val visibility: Visibility = Visibility.None,
    val body: String? = null
) : Identifiable(id(nameId)), AsRust, AttrList {

    companion object {
        fun missingDoc(id: String) = "TODO: Document $id"
    }

    override val attrs: List<Attr>
        get() = TODO("Not yet implemented")

}