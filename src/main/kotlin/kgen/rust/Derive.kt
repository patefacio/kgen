package kgen.rust

/** Enumerated list of rust derives */
enum class Derive {
    /** Support for [equality comparisons](https://doc.rust-lang.org/std/cmp/trait.Eq.html) */
    Eq,

    /** Support for [equality comparisons](https://doc.rust-lang.org/std/cmp/trait.PartialEq.html)
     *  among values that have a [partial equivalence relation](https://en.wikipedia.org/wiki/Partial_equivalence_relation).
     *  Useful for structs with floats that have modeled NaN which is not as straightforward
     *  in terms of equality.
     */
    PartialEq,

    /** Support for a [std::cmp::Ord](https://doc.rust-lang.org/std/cmp/trait.Ord.html#) which
     * are those that have a [total order](https://en.wikipedia.org/wiki/Total_order)
     */
    Ord,

    /** Support for [std::clone::Clone](https://doc.rust-lang.org/std/clone/trait.Clone.html). */
    Clone,

    /** Support for [std::clone::Copy](https://doc.rust-lang.org/core/marker/trait.Copy.html). */
    Copy,

    /** Support for [std::hash::Hash](https://doc.rust-lang.org/std/hash/trait.Hash.html) trait */
    Hash,

    /** Support for [std::default::Default](https://doc.rust-lang.org/std/default/trait.Default.html) trait */
    Default,

    /** Support for (num_traits::identities::Zero)[https://docs.rs/num-traits/0.2.17/num_traits/identities/trait.Zero.html] */
    Zero,

    /** Support for [std::fmt::Debug](https://doc.rust-lang.org/std/fmt/trait.Debug.html) trait */
    Debug,

    /** Support for serde Serialize */
    Serialize,

    /** Support for serde Deserialize */
    Deserialize,

    /** Support for Fail derive */
    Fail,

    /** Support for transforming type into [clap parser](https://docs.rs/clap/latest/clap/_derive/index.html) */
    Parser,

    /** Support for transforming a rust enum into a [clap parser]() */
    ArgEnum
}

/** Returns an attribute set for derives (eg `#[derive(...)]`). */
fun derive(vararg derives: Derive) = Attr.Words(
    "derive",
    derives.map { it.toString() }
)