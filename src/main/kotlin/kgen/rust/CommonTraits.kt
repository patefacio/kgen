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