package kgen.rust

interface Type : AsRust {
    val type: String
    override val asRust get() = type
    val hasRef get() = false
    val isRef get() = false
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

object RustString : Type {
    override val type get() = "String"
}

object Str : Type {
    override val type get() = "str"
}

