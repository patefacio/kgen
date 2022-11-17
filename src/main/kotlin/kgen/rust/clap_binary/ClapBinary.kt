package kgen.rust.clap_binary

import kgen.Identifier
import kgen.doubleQuote
import kgen.id
import kgen.missingDoc
import kgen.rust.*

data class ClapBinary(
    val nameId: String,
    val brief: String = missingDoc(nameId, "Binary About"),
    val doc: String? = null,
    val clapArgs: List<ClapArg> = emptyList(),
    val subcommands: List<ClapCommand> = emptyList(),
    val submodules: List<Module> = emptyList(),
    val hasLogLevel: Boolean = false,
) : Identifier(nameId) {

    val module
        get() = Module(
            nameId,
            brief,
            modules = submodules,
            functions = listOf(
                Fn(
                    "main_run", "Bulk of work for main - placed in fn for consistent error handling.",
                    FnParam("cli", "Cli".asType, "Command line options."),
                    returnDoc = "An application error converted from a std compatible error",
                    returnType = "anyhow::Result<()>".asType
                ),
                Fn(
                    "main", "Main entrypoint for $nameId",
                    body = FnBody(
                        """
let cli = Cli::parse();
main_run(cli).with_context(|| "main_run has failed")?;
Ok(())
""".trimIndent()
                    ),
                    returnType = "anyhow::Result<()>".asType,
                    returnDoc = null
                ),
            ),
            structs = listOf(
                Struct(
                    "cli", doc = "",
                    fields = clapArgs.map { it.field },
                    attrs = derive("Parser", "Debug") + Attr.Dict(
                        "command",
                        listOfNotNull(
                            "about" to brief,
                            if (doc != null) {
                                "long_about" to doc
                            } else {
                                null
                            },
                        )
                    )
                )
            ),
            uses = listOf(
                "clap::Parser",
                "anyhow::Context",
            ).map { Use(it) }.toSet()

        )

}

