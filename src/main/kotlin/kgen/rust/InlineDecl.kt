package kgen.rust

/** Models various inline annotations
 * - `Inline` Indicates compiler should inline but at its discretion
 * - `InlineAlways` Indicates compiler should always inline
 * - `InlineNever` Indicates compiler should never inline the fn
 * - `None` Indicates no inline attribute
 */
enum class InlineDecl {
    Inline,
    InlineAlways,
    InlineNever,
    None;

    fun asRust() = when (this) {
        Inline -> Attr.Word("inline")
        InlineAlways -> Attr.Value("inline", "always")
        InlineNever -> Attr.Value("inline", "never")
        None -> throw RuntimeException("No AsRust for Inline.None")
    }
}