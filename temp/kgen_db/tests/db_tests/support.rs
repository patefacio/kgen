//! Support for db tests

////////////////////////////////////////////////////////////////////////////////////
// --- module uses ---
////////////////////////////////////////////////////////////////////////////////////
use bb8::Pool;
use bb8_postgres::PostgresConnectionManager;
use chrono::Duration;
use chrono::NaiveDate;
use chrono::NaiveDateTime;
use std::ops::Add;
use tokio_postgres::NoTls;
use uuid::Uuid;

////////////////////////////////////////////////////////////////////////////////////
// --- statics ---
////////////////////////////////////////////////////////////////////////////////////
/// The pg connection pool for tests
#[cfg(test)]
pub static TEST_DB_POOL: tokio::sync::OnceCell<Pool<PostgresConnectionManager<NoTls>>> =
    tokio::sync::OnceCell::const_new();

////////////////////////////////////////////////////////////////////////////////////
// --- traits ---
////////////////////////////////////////////////////////////////////////////////////
/// Trait to mutate a value for test purposes
pub trait MutateValue {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self);
}

////////////////////////////////////////////////////////////////////////////////////
// --- functions ---
////////////////////////////////////////////////////////////////////////////////////
/// Return the associated static connection pool
///
///   * _return_ - The pool
#[inline]
pub async fn get_pool() -> Pool<PostgresConnectionManager<NoTls>> {
    TEST_DB_POOL.get_or_init(initialize_db_pool).await.clone()
}

/// Initialize the pool connection - called once by `get_or_init`
///
///   * _return_ - The client _singleton_
pub async fn initialize_db_pool() -> Pool<PostgresConnectionManager<NoTls>> {
    let manager = PostgresConnectionManager::new_from_stringlike(
        "host=localhost user=kgen password=kgen dbname=kgen",
        NoTls,
    )
    .unwrap();

    let pool = Pool::builder()
        .max_size(16) // Maximum connections in the pool
        .build(manager)
        .await
        .unwrap();

    pool
}

////////////////////////////////////////////////////////////////////////////////////
// --- trait impls ---
////////////////////////////////////////////////////////////////////////////////////
impl MutateValue for i64 {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = self.add(1);
    }
}

impl MutateValue for u64 {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = self.add(1);
    }
}

impl MutateValue for u32 {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = self.add(1);
    }
}

impl MutateValue for i32 {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = self.add(1);
    }
}

impl MutateValue for i16 {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = self.add(1);
    }
}

impl MutateValue for String {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.push_str("*");
    }
}

impl MutateValue for char {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = (*self as u8 + 1) as char;
    }
}

impl MutateValue for NaiveDate {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = *self + Duration::days(1);
    }
}

impl MutateValue for NaiveDateTime {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = *self + Duration::days(1);
    }
}

impl MutateValue for Uuid {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        let bytes = self.as_bytes();
        let namespace = Uuid::new_v5(&Uuid::NAMESPACE_DNS, b"kgen-test");
        *self = Uuid::new_v5(&namespace, bytes)
    }
}

// α <mod-def support>
// ω <mod-def support>
