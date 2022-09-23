package kgen.rust

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kgen.rust.Access.*

internal class AccessTest {

    @Test
    fun discriminateAccess() {
        fun acceptAccess(access: Access) = when(access) {
            ReadOnly -> "RO"
            ReadWrite -> "RW"
            ReadOnlyRef -> "ROR"
            ReadWriteRef -> "RWR"
            Inaccessible -> "IA"
            Pub -> "pub"
            PubCrate -> "pub(crate)"
            PubSelf -> "pub(self)"
            PubSuper -> "pub(super)"
            None -> ""
            is PubIn -> "pub(${access.inPackage})"
        }

        assertEquals("RO", acceptAccess(ReadOnly))
        assertEquals("RW", acceptAccess(ReadWrite))
        assertEquals("ROR", acceptAccess(ReadOnlyRef))
        assertEquals("RWR", acceptAccess(ReadWriteRef))
        assertEquals("IA", acceptAccess(Inaccessible))
        assertEquals("pub", acceptAccess(Pub))
        assertEquals("pub(crate)", acceptAccess(PubCrate))
        assertEquals("pub(self)", acceptAccess(PubSelf))
        assertEquals("pub(super)", acceptAccess(PubSuper))
        assertEquals("pub(foo)", acceptAccess(PubIn("foo")))


    }

}


