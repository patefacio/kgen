package kgen

fun trailingSpace(text: String, trailing: String = " ") = if (text.isEmpty()) {
    ""
} else {
    "$text$trailing"
}


fun List<String>.joinNonEmpty(separator: CharSequence = "\n") = this.filter { it.isNotEmpty() }.joinToString(separator)