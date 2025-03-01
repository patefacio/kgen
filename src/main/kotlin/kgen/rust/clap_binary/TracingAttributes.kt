package kgen.rust.clap_binary

/**
 * Represents configuration attributes for setting up a tracing subscriber in a Rust application.
 *
 * This class provides options to configure various aspects of the tracing subscriber, such as
 * enabling ANSI colors, including file and line number information, and toggling thread name visibility.
 * It also supports conditional logging level control based on the presence of a logging level flag.
 *
 * @property hasLogLevel Determines whether log levels are explicitly set for the tracing subscriber. Defaults to `true`.
 * @property tracingWithAnsiColor Enables ANSI color formatting for logs in the tracing subscriber. Defaults to `false`.
 * @property tracingWithTarget Includes the log target (e.g., module or system name) in the output. Defaults to `false`.
 * @property tracingWithThreadNames Includes thread names in the log output. Defaults to `false`.
 * @property tracingWithFile Includes the file name in the log output. Defaults to `false`.
 * @property tracingWithLineNumber Includes the line number in the log output. Defaults to `false`.
 */
data class TracingAttributes(
    val hasLogLevel: Boolean = true,
    val tracingWithAnsiColor: Boolean = false,
    val tracingWithTarget: Boolean = false,
    val tracingWithThreadNames: Boolean = false,
    val tracingWithFile: Boolean = false,
    val tracingWithLineNumber: Boolean = false
) {

    /**
     * Generates the Rust code for initializing a tracing subscriber with the configured attributes.
     *
     * If `hasLogLevel` is `true`, the subscriber is configured with maximum log levels that match
     * the provided CLI `log_level`.
     *
     * @return A string representing the Rust code to initialize the tracing subscriber.
     *
     * Example output when `hasLogLevel = true`:
     * ```
     * tracing::subscriber::set_global_default(
     *     tracing_subscriber::fmt()
     *         .with_ansi(false)
     *         .with_target(false)
     *         .with_file(false)
     *         .with_thread_names(false)
     *         .with_line_number(false)
     *         .with_max_level(match cli.log_level {
     *             LogLevel::Error => tracing::Level::ERROR,
     *             LogLevel::Warn => tracing::Level::WARN,
     *             LogLevel::Info => tracing::Level::INFO,
     *             LogLevel::Debug => tracing::Level::DEBUG,
     *             LogLevel::Trace => tracing::Level::TRACE,
     *         })
     *         .finish(),
     * )
     * .expect("Need to log");
     * ```
     *
     * Example output when `hasLogLevel = false`:
     * ```
     * tracing_subscriber::fmt::init();
     * ```
     */
    val tracingSubscriber
        get() = listOfNotNull(
            """
tracing::subscriber::set_global_default(
    tracing_subscriber::fmt()
        .with_ansi($tracingWithAnsiColor)
        .with_target($tracingWithTarget)
        .with_file($tracingWithFile)
        .with_thread_names($tracingWithThreadNames)
        .with_line_number($tracingWithLineNumber)
        """,
            if (hasLogLevel) {
                """.with_max_level(match cli.log_level { 
                    |LogLevel::Error => tracing::Level::ERROR, 
                    |LogLevel::Warn => tracing::Level::WARN, 
                    |LogLevel::Info => tracing::Level::INFO, 
                    |LogLevel::Debug => tracing::Level::DEBUG, 
                    |LogLevel::Trace => tracing::Level::TRACE, 
                    |})""".trimMargin()
            } else {
                null
            },
            """.finish()
                |)
                |.expect("Need to log")
            """.trimMargin(),
        ).joinToString("\n")
}
