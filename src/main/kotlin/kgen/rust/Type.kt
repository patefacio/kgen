package kgen.rust

import kgen.asSnake

/**
 * Interface common to most rust type items that supports accessing it
 * `asRust` (i.e. suitable for insertion into generated code).
 */
interface Type : AsRust {
    companion object {
        val nonTextRegex = """\W+""".toRegex()
        val trailingUnderbarsRegex = """_+$""".toRegex()
        val whiteSpaceRegex = """\w+""".toRegex()
        val lifetimeRegex = """'\w+""".toRegex()
        val emptyBracketsRegex = """<\s*>""".toRegex()
    }

    val type: String
    override val asRust get() = type

    val asRustName get() = asRust

    val hasRef get() = false
    val isRef get() = false

    fun asRef(lifetime: Lifetime? = null) = Ref(this, lifetime)
    fun asRef(lifetime: String? = null) = Ref(this, lifetime?.asLifetime)

    val doc: String? get() = null

    /** Removes special characters `<`, `>` and non word characters.
     * Used to generate unique test module names for type trait impls.
     */
    val sanitized
        get() = type
            .replace(whiteSpaceRegex) { match ->
                match.value.asSnake
            }
            .replace("<", "_")
            .replace(">", "_")
            .replace(nonTextRegex, "")
            .replace(trailingUnderbarsRegex, "")

    val sanitizedSpecial
        get() = type
            .replace(lifetimeRegex, "")
            .replace(emptyBracketsRegex, "")
            .replace("<", "[")
            .replace(">", "]")
            .replace(trailingUnderbarsRegex, "")
}

object I8 : Type {
    override val type get() = "i8"
}

object I16 : Type {
    override val type get() = "i16"
}

object I32 : Type {
    override val type get() = "i32"
}

object I64 : Type {
    override val type get() = "i64"
}

object U8 : Type {
    override val type get() = "u8"
}

object U16 : Type {
    override val type get() = "u16"
}

object U32 : Type {
    override val type get() = "u32"
}

object U64 : Type {
    override val type get() = "u64"
}

object USize : Type {
    override val type get() = "usize"
}

object ISize : Type {
    override val type get() = "isize"
}

object F64 : Type {
    override val type: String get() = "f64"
}

object RustString : Type {
    override val type get() = "String"
}

object RustBoolean : Type {
    override val type get() = "bool"
}

object Str : Type {
    override val type get() = "str"
}

object Self : Type {
    override val type: String get() = "Self"
}

object RefSelf : Type {
    override val type: String get() = "& Self"
}

object RefMutSelf : Type {
    override val type: String get() = "& mut Self"
}

val Type.asRefMut get() = Ref(this, null, isMutable = true)
