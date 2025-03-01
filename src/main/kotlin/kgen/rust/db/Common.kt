package kgen.rust.db

import kgen.rust.*

/**
 * Represents a function parameter for the database client in Rust.
 *
 * This parameter is a reference to a generic client of type `C`, which must implement
 * the `tokio_postgres::GenericClient` trait. It is typically used to interact with the database
 * through query execution or transaction management.
 */
val clientFnParam = FnParam(
    "client",
    "&C".asType,
    "The tokio postgresql client"
)

/**
 * Represents a generic parameter set for Rust functions that require a database client.
 *
 * This parameter set includes a single generic type parameter `C`, which is bounded by
 * the `tokio_postgres::GenericClient` trait. This ensures that the type used for the client
 * adheres to the necessary interface for executing queries and interacting with the database.
 */
val genericClientParamSet = GenericParamSet(
    TypeParam("c", bounds = Bounds("tokio_postgres::GenericClient"))
)
