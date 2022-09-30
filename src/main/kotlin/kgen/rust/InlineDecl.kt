package kgen.rust

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