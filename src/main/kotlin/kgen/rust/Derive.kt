package kgen.rust

import kgen.asId
import kgen.words

enum class Derive {
    Eq,
    PartialEq,
    Ord,
    Clone,
    Copy,
    Hash,
    Default,
    Zero,
    Debug,

    Serialize,
    Deserialize,
    Fail,

    // Clap
    Parser,
    ArgEnum
}

fun derive(vararg derives: Derive) = Attr.Words(
    "derive",
    derives.map { it.toString() }
)