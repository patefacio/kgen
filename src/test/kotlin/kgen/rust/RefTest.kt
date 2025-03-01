package kgen.rust

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RefTest {

    @Test
    fun getType() {
        assertEquals(
            "&i8",
            Ref(I8).asRust
        )

        assertEquals(
            "&mut i8",
            Ref(I8, isMutable = true).asRust
        )

        assertEquals(
            "&mut Foo",
            Ref("Foo".asType, isMutable = true).asRust
        )

        assertEquals(
            "&'a mut Foo",
            Ref("Foo".asType, lifetime = "a".asLifetime, isMutable = true).asRust
        )

        assertEquals(
            "&'a mut str",
            StrRef(isMutable = true, lifetime = "a".asLifetime).asRust
        )
    }
}