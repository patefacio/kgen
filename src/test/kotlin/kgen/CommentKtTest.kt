package kgen

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CommentKtTest {

    val text = """
            this is
            
            
            a test
            
            
        """.trimIndent()

    @Test
    fun comment() {
        assertEquals("""
            // this is
            // 
            // 
            // a test
        """.trimIndent(),
        comment(text))

        assertEquals("""
            /// this is
            /// 
            /// 
            /// a test
        """.trimIndent(),
            commentTriple(text))

        assertEquals("""
            # this is
            # 
            # 
            # a test
        """.trimIndent(),
            commentScript(text)
        )
    }
}