package kgen.rust

import kgen.alphaOmegaDelimiter


/**
 * Model the body of a rust function. Choose between a `body` or `emptyBlockName`.
 * Decorate with `preBlock` and or `postBlock` as desired.
 *
 * @body Main contents of the function
 * @preBlock Content placed before the `body` or `emptyBlock` identified by
 * `emptyBlockName`.
 * @postBlock Content placed after the `body` or `emptyBlock` identified by
 * `emptyBlockName`.
 * @emptyBlockName If set will generate protection block named `emptyBlockName`
 * with any `preBlock` and `postBlock` content around the empty block.
 */
data class FnBody(
    val body: String? = null,
    val preBlock: String? = null,
    val postBlock: String? = null,
    val emptyBlockName: String? = null
) : AsRust {

    /** The function body as rust code */
    override val asRust: String
        get() = listOfNotNull(
            preBlock,
            when {
                body != null -> body
                emptyBlockName != null -> alphaOmegaDelimiter.emptyBlock(emptyBlockName)
                else -> null
            },
            postBlock
        )
            .joinToString("\n")
}

val String.asFnBody get() = FnBody(this)