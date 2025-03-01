package kgen.rust.clap_binary

import kgen.Identifier
import kgen.missingDoc
import kgen.rust.*
import kgen.rust.Enum

/**
 * Represents a CLI binary built using the `Clap` Rust library.
 *
 * This class generates the necessary Rust components for building a command-line interface (CLI) binary,
 * including handling arguments, subcommands, structured logging, and more. It provides flexible configuration
 * options for setting up the binary's functionality.
 *
 * @property nameId The unique identifier (name) of the binary.
 * @property brief A brief description of the binary, shown in CLI help. Defaults to a placeholder description.
 * @property doc Optional long-form documentation for the binary, shown in CLI help under `long_about`.
 * @property clapArgs A list of arguments for the CLI. Defaults to an empty list.
 * @property subcommands A list of subcommands supported by the CLI. Defaults to an empty list.
 * @property submodules A list of Rust modules associated with the binary. Defaults to an empty list.
 * @property hasLogLevel Indicates whether the binary supports log level selection (e.g., debug, info). Defaults to `true`.
 * @property mainRunBody The body of the main function (`main_run`), where the core logic of the binary is implemented.
 *        Defaults to `null` for no custom logic.
 * @property structs A list of additional Rust structs used by the binary. Defaults to an empty list.
 * @property enums A list of additional Rust enums used by the binary. Defaults to an empty list.
 * @property functions A list of additional functions defined within the binary. Defaults to an empty list.
 * @property staticInits A list of static initializations used by the binary. Defaults to an empty list.
 * @property lazies A list of lazily initialized values in the binary. Defaults to an empty list.
 * @property uses A set of Rust `use` statements required by the binary. Defaults to an empty set.
 * @property inSeparateDirectory Indicates whether the binary is located in a separate directory. Defaults to `false`.
 * @property useTokioMain Indicates whether the `#[tokio::main]` attribute is used for asynchronous operations. Defaults to `false`.
 * @property asyncMain Indicates whether the main function is asynchronous. Defaults to `false`.
 * @property tracingAttributes Configuration attributes for the tracing subscriber. Defaults to `null` for no tracing configuration.
 * @property tracingInitializer A lambda function to initialize the tracing subscriber using the provided `TracingAttributes`.
 *        Defaults to a function that uses the `tracingSubscriber` from the given `TracingAttributes`.
 */
data class ClapBinary(
    val nameId: String,
    val brief: String = missingDoc(nameId, "Binary About"),
    val doc: String? = null,
    val clapArgs: List<ClapArg> = emptyList(),
    val subcommands: List<ClapCommand> = emptyList(),
    val submodules: List<Module> = emptyList(),
    val hasLogLevel: Boolean = true,
    val mainRunBody: FnBody? = null,
    val structs: List<Struct> = emptyList(),
    val enums: List<Enum> = emptyList(),
    val functions: List<Fn> = emptyList(),
    val staticInits: List<StaticInit> = emptyList(),
    val lazies: List<Lazy> = emptyList(),
    val uses: Set<Use> = emptySet(),
    val inSeparateDirectory: Boolean = false,
    val useTokioMain: Boolean = false,
    val asyncMain: Boolean = false,
    val tracingAttributes: TracingAttributes? = null,
    val tracingInitializer: (TracingAttributes) -> String = { tracingAttributes -> tracingAttributes.tracingSubscriber }
) : Identifier(nameId) {

    /**
     * Determines if the main function is asynchronous, based on the configuration of `useTokioMain` or `asyncMain`.
     */
    private val mainIsAsync get() = useTokioMain || asyncMain

    /**
     * Returns the log level argument for the CLI, if log level support is enabled.
     */
    private val logLevelArg
        get() = if (hasLogLevel) {
            standardLogLevelArg
        } else {
            null
        }

    /**
     * Generates the `Module` that represents the binary, including its functions, enums, and structs.
     *
     * This module includes:
     * - The `main` function as the entry point to the CLI.
     * - The `main_run` function that handles the core logic.
     * - Support for command-line arguments and subcommands.
     * - Additional enums (e.g., log levels) and structs (e.g., CLI argument struct).
     * - Support for tracing initialization if configured.
     */
    val module
        get() = Module(
            nameId,
            brief,
            modules = submodules,
            staticInits = staticInits,
            lazies = lazies,
            functions = functions + listOf(
                Fn(
                    "main_run", "Bulk of work for main - placed in fn for consistent error handling.",
                    FnParam("cli", "Cli".asType, "Command line options.", allowUnused = true),
                    returnDoc = "An application error converted from a std compatible error",
                    returnType = "anyhow::Result<()>".asType,
                    body = mainRunBody,
                    isAsync = mainIsAsync
                ),
                Fn(
                    "main",
                    "Main entrypoint for $nameId",
                    body = FnBody(
                        """
let cli = Cli::parse();

${tracingInitializer(tracingAttributes ?: TracingAttributes())}

main_run(cli)${
                            if (mainIsAsync) {
                                ".await"
                            } else {
                                ""
                            }
                        }.with_context(|| "main_run has failed")?;
Ok(())
""".trimIndent()
                    ),
                    returnType = "anyhow::Result<()>".asType,
                    returnDoc = null,
                    visibility = Visibility.None,
                    isAsync = mainIsAsync,
                    attrs = if (useTokioMain) {
                        attrTokioMain.asAttrList
                    } else {
                        AttrList()
                    },
                ),
            ),
            enums = enums + listOfNotNull(
                if (hasLogLevel) {
                    Enum(
                        "log_level",
                        "For setting log level to include specified level (Mappings to `tracing::Level`) and more grave.",
                        Variant.UnitStruct("error", "An error"),
                        Variant.UnitStruct("warn", "A warning"),
                        Variant.UnitStruct("info", "An info message"),
                        Variant.UnitStruct("debug", "Debug messages"),
                        Variant.UnitStruct("trace", "Trace messages"),
                        attrs = derive("Debug", "Copy", "Clone", "ValueEnum")
                    )
                } else {
                    null
                },
                if (subcommands.isNotEmpty()) {
                    Enum(
                        "command", "The supported commands.",
                        subcommands.map { clapCommand ->
                            Variant.Struct(
                                clapCommand.nameId, clapCommand.doc,
                                clapCommand.clapArgs.map { it.field }
                            )
                        },
                        attrs = derive("Subcommand", "Debug"),
                        uses = "clap::Subcommand".asUses
                    )
                } else {
                    null
                }
            ),
            structs = structs + listOf(
                Struct(
                    "cli", doc = "",
                    fields = (clapArgs + listOfNotNull(logLevelArg)).map { it.field } + if (subcommands.isNotEmpty()) {
                        listOf(
                            Field(
                                "command", "The supported commands.",
                                "Command".asType,
                                attrs = clapSubcommand.asAttrList
                            )
                        )
                    } else {
                        emptyList()
                    },
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
            uses = uses + listOfNotNull(
                "clap::Parser",
                if (hasLogLevel) {
                    "clap::ValueEnum"
                } else {
                    null
                },
                "anyhow::Context",
            ).asUses,
            isBinary = true
        )
}
