package kgen.rust

import kgen.*

class CargoToml(
    val nameId: String,
    val description: String = "TODO: Describe cargo project($nameId)",
    val version: String = "0.0.1",
    val authors: List<String> = emptyList(),
    val homepage: String? = null,
    val license: String? = "MIT",
    val dependencies: List<String> = emptyList(),
    val edition: String = "2021",
    val keywords: List<String> = emptyList()
) : Identifiable(nameId) {

    val toml
        get() = listOfNotNull(
            """
            [package]
            edition = $edition
            name = $nameId
            version = $version
            description = ${tripleQuote(description)}
            license = "$license"
            keywords = ${keywords.map { doubleQuote(it) }}
        """.trimIndent(),
            if (authors.isEmpty()) {
                null
            } else {
                "authors: ${authors.joinToString()}"
            },
            listOfNotNull(
                "\n[dependencies]",
                if (dependencies.isEmpty()) {
                    null
                } else {
                    trailingSpace(dependencies.joinToString("\n"))
                },
                emptyBlock("dependencies", scriptDelimiter)
            ).joinToString("\n")
        ).joinToString("\n")

}