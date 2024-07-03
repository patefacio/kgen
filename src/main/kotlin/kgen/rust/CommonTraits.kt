package kgen.rust

/** This file contains trait definitions of common rust traits. Use these
 * to easily add trait implementations to your types.
 */

/** `std::default::Default` Trait https://doc.rust-lang.org/nightly/std/default/trait.Default.html
 */
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

/** `std::fmt::Debug` Trait https://doc.rust-lang.org/nightly/std/fmt/trait.Debug.html
 */
val debugTrait = Trait(
    "debug", "Format trait for debug formatting",
    functions = listOf(
        Fn(
            "fmt",
            "Format the instance.l",
            refSelf,
            FnParam(
                "f",
                "&mut Formatter<'_>".asType,
                doc = "The formatter",
                attrs = attrAllowUnused.asAttrList,
            ),
            returnDoc = "Formatted instance",
            returnType = "::core::fmt::Result".asType,
            uses = listOf("::core::fmt::Display", "::core::fmt::Formatter").asUses
        )
    )
)

/** `std::cmp::PartialEq` Trait https://doc.rust-lang.org/nightly/std/cmp/trait.PartialEq.html
 */
val partialEqTrait = Trait(
    "partial_eq",
    "Trait for comparisons using the equality operator.",
    functions = listOf(
        Fn(
            "eq",
            "Returns true if `other` equals `self`.",
            refSelf,
            FnParam("other", "&Self".asType, "Instance to compare"),
            returnDoc = "True if `other` equals `self`.",
            returnType = RustBoolean
        )
    )
)

/** `std::hash::Hash` Trait https://doc.rust-lang.org/nightly/std/hash/trait.Hash.html
 */
val hashTrait = Trait(
    "hash",
    "Trait for hashable types",
    functions = listOf(
        Fn(
            "hash",
            "Feeds this value into the given `Hasher`",
            refSelf,
            FnParam("state", "& mut H".asType, "Hasher to feed values"),
            genericParamSet = GenericParamSet(
                TypeParam("h", bounds = Bounds("std::hash::Hasher"))
            )
        ),
    )
)

/** `std::ops::AddAssign` Trait https://doc.rust-lang.org/nightly/std/ops/trait.AddAssign.html
 */
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

/** `std::ops::MulAssign` Trait https://doc.rust-lang.org/nightly/std/ops/trait.MulAssign.html
 */
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

/** `std::ops::Mul` Trait https://doc.rust-lang.org/nightly/std/ops/trait.Mul.html
 */
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


val notTrait = Trait(
    "not",
    "The unary negation operator `-`",
    functions = listOf(
        Fn(
            "not",
            "The unary logical negation operator !.",
            self,
            returnDoc = "The new negated `Self`",
            returnType = "Self::Output".asType
        )
    ),
    associatedTypes = listOf(
        AssociatedType("output", "The output type of the operation.")
    ),
    uses = listOf("std::ops::Not").asUses
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
            "Formatter to push formatted item to.",
            attrs = attrAllowUnused.asAttrList
        ),
        returnDoc = "Formatted instance",
        returnType = "::core::fmt::Result".asType,
        uses = listOf("::core::fmt::Display", "::core::fmt::Formatter").asUses
    )
)

val cloneTrait = Trait(
    "clone",
    "A common trait for the ability to explicit duplicate an object.",
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

val dropTrait = Trait(
    "drop",
    "Called when value is dropped",
    Fn(
        "drop",
        "Custom code within the destructor",
        refMutSelf
    )
)

private val glooWorkerScopeParam = FnParam(
    "scope",
    "& WorkerScope<Self>".asType,
    "The scope of the worker"
)

val glooWorkerTrait = Trait(
    "worker",
    "Support for web worker",
    Fn(
        "create",
        "Create the worker",
        glooWorkerScopeParam,
        returnType = "Self".asType,
        returnDoc = "The created worker"
    ),
    Fn(
        "update",
        "Worker receives an update",
        refMutSelf,
        glooWorkerScopeParam,
        FnParam(
            "msg",
            "Self::Message".asType
        )
    ),
    Fn(
        "received",
        "Receives an input from a connected bridge",
        refMutSelf,
        glooWorkerScopeParam,
        FnParam(
            "msg",
            "Self::Input".asType
        ),
        FnParam(
            "id",
            "HandlerId".asType,
            "The handler id"
        )
    ),
    associatedTypes = listOf(
        AssociatedType("message"),
        AssociatedType("input"),
        AssociatedType("output")
    )
)

val allCommonTraits = setOf(
    defaultTrait, partialEqTrait, addAssignTrait, mulAssignTrait, mulTrait,
    negTrait, notTrait, iteratorTrait, intoIteratorTrait, displayTrait, cloneTrait,
    sendTrait, syncTrait, dropTrait
)