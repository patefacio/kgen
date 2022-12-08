package kgen.rust


val defaultTrait = Trait(
    "default", "Rust default trait",
    functions = listOf(
        Fn(
            "default", "A trait for giving a type a useful default value.",
            returnDoc = "The new default instance",
            returnType = "Self".asType
        )
    )
)