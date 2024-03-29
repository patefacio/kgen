package kgen.rust

import kgen.*

class CargoToml(
    val nameId: String,
    val description: String = "TODO: Describe cargo project($nameId)",
    val version: String = "0.0.1",
    val authors: List<String> = emptyList(),
    val homepage: String? = null,
    val license: String = "MIT",
    val dependencies: List<String> = emptyList(),
    val buildDependencies: List<String> = emptyList(),
    val edition: String = "2021",
    val keywords: List<String> = emptyList()
) : Identifier(nameId) {

    val toml
        get() = listOfNotNull(
            """
            [package]
            edition = ${doubleQuote(edition)}
            name = ${doubleQuote(nameId)}
            version = ${doubleQuote(version)}
            description = ${tripleQuote(description)}
            license = ${doubleQuote(license)}
            keywords = ${keywords.map { doubleQuote(it) }}
            
            """.trimIndent(),
            if (authors.isEmpty()) {
                null
            } else {
                "authors: ${authors.joinToString()}"
            },
            listOfNotNull(
                "\n[lib]",
                emptyOpenDelimitedBlock("lib", scriptDelimiter),
                "\n[dependencies]",
                if (dependencies.isEmpty()) {
                    null
                } else {
                    trailingText(dependencies.joinToString("\n"))
                },
                emptyOpenDelimitedBlock("dependencies", scriptDelimiter),
                "\n[build-dependencies]",
                if (buildDependencies.isEmpty()) {
                    null
                } else {
                    trailingText(buildDependencies.joinToString("\n"))
                },
                emptyOpenDelimitedBlock("build-dependencies", scriptDelimiter),
                "\n[features]",
                emptyOpenDelimitedBlock("features", scriptDelimiter),
            ).joinToString("\n"),
            emptyOpenDelimitedBlock("additional", scriptDelimiter),
        ).joinToString("\n")

}