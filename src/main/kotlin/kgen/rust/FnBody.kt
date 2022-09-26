package kgen.rust

data class FnBody(
    val body: String,
    val preBlock: String? = null,
    val postBlock: String? = null
) : AsRust {

    override val asRust: String
        get() = listOfNotNull(
            preBlock,
            body,
            postBlock
        )
            .joinToString("\n")

}