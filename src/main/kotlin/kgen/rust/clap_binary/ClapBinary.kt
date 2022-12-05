package kgen.rust.clap_binary

import kgen.Identifier
import kgen.missingDoc
import kgen.rust.*
import kgen.rust.Enum

data class ClapBinary(
    val nameId: String,
    val brief: String = missingDoc(nameId, "Binary About"),
    val doc: String? = null,
    val clapArgs: List<ClapArg> = emptyList(),
    val subcommands: List<ClapCommand> = emptyList(),
    val submodules: List<Module> = emptyList(),
    val hasLogLevel: Boolean = true,
    val mainRunBody: FnBody? = null,
    val functions: List<Fn> = emptyList(),
    val staticInits: List<StaticInit> = emptyList(),
    val uses: Set<Use> = emptySet(),
    val inSeparateDirectory: Boolean = false
) : Identifier(nameId) {

    private val logLevelArg
        get() = if (hasLogLevel) {
            ClapArg(
                "log_level",
                "Log-level for the run.",
                type = "LogLevel".asType,
                defaultLiteralValue = "LogLevel::Warn",
                longName = "ll",
                isEnum = true
            )
        } else {
            null
        }

    private val initializeLogger get() = if(hasLogLevel) {
        """
tracing::subscriber::set_global_default(
    tracing_subscriber::fmt()
        .with_file(true)
        .with_line_number(true)            
        .with_max_level(match cli.log_level {
            LogLevel::Error => tracing::Level::ERROR,
            LogLevel::Warn => tracing::Level::WARN,
            LogLevel::Info => tracing::Level::INFO,
            LogLevel::Debug => tracing::Level::DEBUG,
            LogLevel::Trace => tracing::Level::TRACE,
        })   
        .finish(),
)
.expect("Need to log");                 
        """.trimIndent()
    } else {
        "tracing_subscriber::fmt::init();"
    }

    val module
        get() = Module(
            nameId,
            brief,
            modules = submodules,
            staticInits = staticInits,
            functions = functions + listOf(
                Fn(
                    "main_run", "Bulk of work for main - placed in fn for consistent error handling.",
                    FnParam("cli", "Cli".asType, "Command line options."),
                    returnDoc = "An application error converted from a std compatible error",
                    returnType = "anyhow::Result<()>".asType,
                    body = mainRunBody
                ),
                Fn(
                    "main", "Main entrypoint for $nameId",
                    body = FnBody(
                        """
let cli = Cli::parse();

$initializeLogger

main_run(cli).with_context(|| "main_run has failed")?;
Ok(())
""".trimIndent()
                    ),
                    returnType = "anyhow::Result<()>".asType,
                    returnDoc = null,
                    visibility = Visibility.None
                ),
            ),
            enums = listOfNotNull(
                if (hasLogLevel) {
                    Enum(
                        "log_level",
                        "For setting log level to include specified level (Mappings to `tracing::Level`) and more grave.",
                        EnumValue.UnitStruct("error", "An error"),
                        EnumValue.UnitStruct("warn", "A warning"),
                        EnumValue.UnitStruct("info", "An info message"),
                        EnumValue.UnitStruct("debug", "Debug messages"),
                        EnumValue.UnitStruct("trace", "Trace messages"),
                        attrs = derive("Debug", "Copy", "Clone", "ValueEnum")
                    )
                } else {
                    null
                },
            ),
            structs = listOf(
                Struct(
                    "cli", doc = "",
                    fields = (clapArgs + listOfNotNull(logLevelArg)).map { it.field },
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

