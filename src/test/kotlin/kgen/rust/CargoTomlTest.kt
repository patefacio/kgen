package kgen.rust

import kgen.tripleQuote
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CargoTomlTest {

    @Test
    fun getToml() {

        assertEquals(
            """
[package]
edition = "2021"
name = "foo_bar"
version = "0.0.1"
description = ""${'"'}Project foo bar""${'"'}
license = "MIT"
keywords = []

[dependencies]
# α <dependencies>
# ω <dependencies>

[build-dependencies]
# α <build-dependencies>
# ω <build-dependencies>
""".trimIndent(),
            CargoToml("foo_bar", "Project foo bar").toml
        )
    }
}