package kgen.rust

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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


[lib]
# α <lib>
# ω <lib>

[dependencies]
# α <dependencies>
# ω <dependencies>

[build-dependencies]
# α <build-dependencies>
# ω <build-dependencies>

[features]
# α <features>
# ω <features>
# α <additional>
# ω <additional>
""".trimIndent(),
            CargoToml("foo_bar", "Project foo bar").toml
        )
    }
}