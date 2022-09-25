package kgen.rust

fun mutable(isMutable: Boolean) = if(isMutable) {
    "mut"
} else {
    ""
}