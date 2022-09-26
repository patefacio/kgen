package kgen

fun trailingSpace(text: String, trailing: String = " ") = if (text.isEmpty()) {
    ""
} else {
    "$text$trailing"
}


fun List<String>.joinNonEmpty(separator: CharSequence = "\n") = this.filter { it.isNotEmpty() }.joinToString(separator)

fun indent(text: String?, indent: String = "  ") = text
    ?.split("\n")
    ?.joinToString("\n") { "$indent$it" }


