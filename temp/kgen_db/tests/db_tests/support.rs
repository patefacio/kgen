//! Support for db tests

////////////////////////////////////////////////////////////////////////////////////
// --- module uses ---
////////////////////////////////////////////////////////////////////////////////////
use chrono::Duration;
use chrono::NaiveDate;
use chrono::NaiveDateTime;
use deadpool_postgres::Config;
use deadpool_postgres::ManagerConfig;
use deadpool_postgres::Pool;
use deadpool_postgres::RecyclingMethod;
use deadpool_postgres::Runtime;
use serde_json::Value;
use std::ops::Add;
use tokio_postgres::NoTls;
use uuid::Uuid;

////////////////////////////////////////////////////////////////////////////////////
// --- statics ---
////////////////////////////////////////////////////////////////////////////////////
/// The pg connection pool for tests
#[cfg(test)]
pub static TEST_DB_POOL: tokio::sync::OnceCell<Pool> = tokio::sync::OnceCell::const_new();

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
pub async fn get_pool() -> Pool {
    TEST_DB_POOL.get_or_init(initialize_db_pool).await.clone()
}

/// Initialize the pool connection - called once by `get_or_init`
///
///   * _return_ - The client _singleton_
pub async fn initialize_db_pool() -> Pool {
    let mut cfg = Config::new();
    cfg.dbname = Some("kgen".to_string());
    cfg.user = Some("kgen".to_string());
    cfg.password = Some("kgen".to_string());
    cfg.manager = Some(ManagerConfig {
        recycling_method: RecyclingMethod::Fast,
    });
    cfg.create_pool(Some(Runtime::Tokio1), NoTls).unwrap()
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

impl MutateValue for Option<i64> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.as_mut().map(|v| v.add(1));
    }
}

impl MutateValue for u64 {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = self.add(1);
    }
}

impl MutateValue for Option<u64> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.as_mut().map(|v| v.add(1));
    }
}

impl MutateValue for u32 {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = self.add(1);
    }
}

impl MutateValue for Option<u32> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.as_mut().map(|v| v.add(1));
    }
}

impl MutateValue for i32 {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = self.add(1);
    }
}

impl MutateValue for Option<i32> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.as_mut().map(|v| v.add(1));
    }
}

impl MutateValue for i16 {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = self.add(1);
    }
}

impl MutateValue for Option<i16> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.as_mut().map(|v| v.add(1));
    }
}

impl MutateValue for String {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.push_str("*");
    }
}

impl MutateValue for Option<String> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.as_mut().map(|v| v.push_str("*"));
    }
}

impl MutateValue for Value {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        // Need easy mutation
    }
}

impl MutateValue for Option<Value> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        // Need easy way to mutate
    }
}

impl MutateValue for char {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = (*self as u8 + 1) as char;
    }
}

impl MutateValue for Option<char> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.as_mut().map(|c| *c = (*c as u8 + 1) as char);
    }
}

impl MutateValue for bool {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = !*self;
    }
}

impl MutateValue for Option<bool> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.as_mut().map(|v| *v = !*v);
    }
}

impl MutateValue for NaiveDate {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = *self + Duration::days(1);
    }
}

impl MutateValue for Option<NaiveDate> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.as_mut().map(|v| *v = *v + Duration::days(1));
    }
}

impl MutateValue for NaiveDateTime {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        *self = *self + Duration::days(1);
    }
}

impl MutateValue for Option<NaiveDateTime> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.as_mut().map(|v| *v = *v + Duration::days(1));
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

impl MutateValue for Option<Uuid> {
    /// Change the value in some deterministic way
    fn mutate_value(&mut self) {
        self.as_mut().map(|u| u.mutate_value());
    }
}

// α <mod-def support>
// ω <mod-def support>
