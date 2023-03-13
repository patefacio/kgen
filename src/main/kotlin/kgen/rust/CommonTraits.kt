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

val addAssignTrait = Trait(
    "add_assign",
    "The addition assignment operator `+=`",
    functions = listOf(
        Fn(
            "add_assign",
            "Add `Rhs` to `self`",
            refMutSelf,
            FnParam("rhs", "Rhs".asType, "Right hand side")
        )
    ),
    genericParamSet = GenericParamSet(
        typeParams = listOf(
            TypeParam("rhs", "Self".asType)
        )
    ),
    uses = listOf("std::ops::AddAssign").asUses
)

val mulAssignTrait = Trait(
    "mul_assign",
    "The multiplication assignment operator `*=`",
    functions = listOf(
        Fn(
            "mul_assign",
            "Multiply `self` by `Rhs`",
            refMutSelf,
            FnParam("rhs", "Rhs".asType, "Right hand side")
        )
    ),
    genericParamSet = GenericParamSet(
        typeParams = listOf(
            TypeParam("rhs", "Self".asType)
        )
    ),
    uses = listOf("std::ops::MulAssign").asUses
)

val mulTrait = Trait(
    "mul",
    "The multiplication operator `*`",
    functions = listOf(
        Fn(
            "mul",
            "Return new `self` multiplied by `Rhs`.",
            self,
            FnParam("rhs", "Rhs".asType, "Right hand side"),
            returnDoc = "The new scaled `Self`",
            returnType = "Self::Output".asType
        )
    ),
    associatedTypes = listOf(
        AssociatedType("output", "The output type of the operation.")
    ),
    genericParamSet = GenericParamSet(
        typeParams = listOf(
            TypeParam("rhs", "Self".asType)
        )
    ),
    uses = listOf("std::ops::Mul").asUses
)

val negTrait = Trait(
    "neg",
    "The unary negation operator `-`",
    functions = listOf(
        Fn(
            "neg",
            "Return negated `self`.",
            self,
            returnDoc = "The new negated `Self`",
            returnType = "Self::Output".asType
        )
    ),
    associatedTypes = listOf(
        AssociatedType("output", "The output type of the operation.")
    ),
    uses = listOf("std::ops::Neg").asUses
)

val iteratorTrait = Trait(
    "iterator",
    "The iterator trait",
    Fn(
        "next",
        "The next item in the sequence.",
        refMutSelf,
        returnDoc = "The next item if available.",
        returnType = "Option<Self::Item>".asType
    ),
    associatedTypes = listOf(
        AssociatedType("item", "The item being iterated over.")
    )
)

val intoIteratorTrait = Trait(
    "into_iterator",
    "The `into_iterator` trait for converting an object into something to iterate over.",
    Fn(
        "into_iter",
        "Consume object and providing to new iterator over its contents.",
        self,
        returnDoc = "The iterator.",
        returnType = "Self::IntoIter".asType
    ),
    associatedTypes = listOf(
        AssociatedType("item", "Type of item being iterated over."),
        AssociatedType("into_iter", "The iterator instance.", Bounds("Iterator<Item = Self::Item>"))
    )
)

val displayTrait = Trait(
    "display",
    "Format trait for empty format.",
    Fn(
        "fmt",
        "Format the instance.",
        //fn fmt(&self, f: &mut Formatter<'_>) -> Result;
        refSelf,
        FnParam(
            "f",
            "&mut Formatter<'_>".asType,
            "Formatter to push formatted item to."
        ),
        returnDoc = "Formatted instance",
        returnType = "core::fmt::Result".asType,
        uses = listOf("core::fmt::Display", "core::fmt::Formatter").asUses
    )
)

val cloneTrait = Trait(
    "clone",
    "A common trait for the ability to explicity duplicate an object.",
    Fn(
        "clone",
        "Clone the instance",
        refSelf,
        returnDoc = "The cloned instance",
        returnType = "Self".asType
    ),
    Fn(
        "clone_from",
        "Clone the instance from source",
        refSelf,
        FnParam("source", "&Self".asType, "The source to clone."),
        returnDoc = "The cloned instance",
        returnType = "Self".asType
    )
)

val sendTrait = Trait("send", "The send marker trait")
val syncTrait = Trait("sync", "The sync marker trait")