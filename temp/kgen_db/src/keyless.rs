//! Table gateway pattern implemented for Keyless

////////////////////////////////////////////////////////////////////////////////////
// --- module uses ---
////////////////////////////////////////////////////////////////////////////////////
use chrono::{NaiveDate, NaiveDateTime};
use tokio_postgres::types::{Date, FromSql, ToSql};

////////////////////////////////////////////////////////////////////////////////////
// --- structs ---
////////////////////////////////////////////////////////////////////////////////////
/// Primary data fields
#[derive(Debug, Clone, Default)]
pub struct KeylessData {
    /// Field for column `the_name`
    pub the_name: String,
    /// Field for column `the_small_int`
    pub the_small_int: i16,
    /// Field for column `the_large_int`
    pub the_large_int: i64,
    /// Field for column `general_int`
    pub general_int: i32,
    /// Field for column `the_date`
    pub the_date: chrono::NaiveDate,
    /// Field for column `the_date_time`
    pub the_date_time: chrono::NaiveDateTime,
    /// Field for column `the_uuid`
    pub the_uuid: uuid::Uuid,
    /// Field for column `the_ulong`
    pub the_ulong: i64,
}

/// Table Gateway Support for table `keyless`.
/// Rows
#[derive(Debug, Clone, Default)]
pub struct TableKeyless {}

////////////////////////////////////////////////////////////////////////////////////
// --- type impls ---
////////////////////////////////////////////////////////////////////////////////////
impl TableKeyless {
    /// Select rows of `keyless` with provided where clause
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **where_clause** - The where clause (sans `where` keyword)
    ///   * **params** - Any clause parameters
    ///   * **capacity** - Capacity to the results
    ///   * _return_ - Selected rows
    pub async fn select_all_where(
        client: &tokio_postgres::Client,
        where_clause: &str,
        params: &[&(dyn ToSql + Sync)],
        capacity: usize,
    ) -> Vec<KeylessData> {
        let statement = format!(
            r#"SELECT 
    the_name, the_small_int, the_large_int, general_int, the_date, the_date_time,
    	the_uuid, the_ulong
    FROM keyless
    WHERE {where_clause}"#
        );
        let mut results = Vec::<KeylessData>::with_capacity(capacity);
        let rows = match client.query(&statement, params).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };

        for row in rows {
            results.push(KeylessData {
                the_name: row.get(0),
                the_small_int: row.get(1),
                the_large_int: row.get(2),
                general_int: row.get(3),
                the_date: row.get(4),
                the_date_time: row.get(5),
                the_uuid: row.get(6),
                the_ulong: row.get(7),
            });
            tracing::info!("{:?}", results.last().unwrap());
        }
        results
    }

    /// Select rows of `keyless`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **capacity** - Capacity to the results
    ///   * _return_ - Selected rows
    #[inline]
    pub async fn select_all(client: &tokio_postgres::Client, capacity: usize) -> Vec<KeylessData> {
        Self::select_all_where(&client, "1=1", &[], capacity).await
    }

    /// Insert rows of `keyless`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Rows to insert
    pub async fn insert(client: &tokio_postgres::Client, rows: &[KeylessData]) {
        todo!()
    }

    /// Insert large batch of [Keyless] rows.
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Rows to insert
    ///   * **chunk_size** - How to chunk the inserts
    ///   * _return_ - TODO: Document FnReturn(bulk_insert)
    pub async fn bulk_insert(
        client: &tokio_postgres::Client,
        rows: &[KeylessData],
        chunk_size: usize,
    ) -> Result<(), tokio_postgres::Error> {
        let mut chunk = 0;
        let mut the_name = Vec::with_capacity(chunk_size);
        let mut the_small_int = Vec::with_capacity(chunk_size);
        let mut the_large_int = Vec::with_capacity(chunk_size);
        let mut general_int = Vec::with_capacity(chunk_size);
        let mut the_date = Vec::with_capacity(chunk_size);
        let mut the_date_time = Vec::with_capacity(chunk_size);
        let mut the_uuid = Vec::with_capacity(chunk_size);
        let mut the_ulong = Vec::with_capacity(chunk_size);
        for chunk_rows in rows.chunks(chunk_size) {
            for row in chunk_rows.into_iter() {
                the_name.push(&row.the_name);
                the_small_int.push(row.the_small_int);
                the_large_int.push(row.the_large_int);
                general_int.push(row.general_int);
                the_date.push(row.the_date);
                the_date_time.push(row.the_date_time);
                the_uuid.push(row.the_uuid);
                the_ulong.push(row.the_ulong);
            }
            let chunk_result = client
                .execute(
                    r#"insert into keyless
    (
    	the_name, the_small_int, the_large_int, general_int, the_date, the_date_time,
    	the_uuid, the_ulong
    )
    SELECT * FROM UNNEST
    (
    	$1::varchar[], $2::smallint[], $3::bigint[], $4::int[], $5::date[], $6::timestamp[],
    	$7::uuid[], $8::bigint[]
    )
    "#,
                    &[
                        &the_name,
                        &the_small_int,
                        &the_large_int,
                        &general_int,
                        &the_date,
                        &the_date_time,
                        &the_uuid,
                        &the_ulong,
                    ],
                )
                .await;

            match &chunk_result {
                Err(err) => {
                    tracing::error!("Failed bulk_insert `keyless` chunk({chunk}) -> {err}");
                    chunk_result?;
                }
                _ => tracing::debug!(
                    "Finished inserting chunk({chunk}), size({}) in `keyless`",
                    chunk_rows.len()
                ),
            }
            chunk += 1;
            the_name.clear();
            the_small_int.clear();
            the_large_int.clear();
            general_int.clear();
            the_date.clear();
            the_date_time.clear();
            the_uuid.clear();
            the_ulong.clear();
        }
        Ok(())
    }

    /// Upsert large batch of [Keyless] rows.
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Rows to insert
    ///   * **chunk_size** - How to chunk the inserts
    ///   * _return_ -
    pub async fn bulk_upsert(
        client: &tokio_postgres::Client,
        rows: &[KeylessData],
        chunk_size: usize,
    ) -> Result<(), tokio_postgres::Error> {
        let mut chunk = 0;
        let mut the_name = Vec::with_capacity(chunk_size);
        let mut the_small_int = Vec::with_capacity(chunk_size);
        let mut the_large_int = Vec::with_capacity(chunk_size);
        let mut general_int = Vec::with_capacity(chunk_size);
        let mut the_date = Vec::with_capacity(chunk_size);
        let mut the_date_time = Vec::with_capacity(chunk_size);
        let mut the_uuid = Vec::with_capacity(chunk_size);
        let mut the_ulong = Vec::with_capacity(chunk_size);
        for chunk_rows in rows.chunks(chunk_size) {
            for row in chunk_rows.into_iter() {
                the_name.push(&row.the_name);
                the_small_int.push(row.the_small_int);
                the_large_int.push(row.the_large_int);
                general_int.push(row.general_int);
                the_date.push(row.the_date);
                the_date_time.push(row.the_date_time);
                the_uuid.push(row.the_uuid);
                the_ulong.push(row.the_ulong);
            }
            let chunk_result = client
                .execute(
                    r#"insert into keyless
    (
    	the_name, the_small_int, the_large_int, general_int, the_date, the_date_time,
    	the_uuid, the_ulong
    )
    SELECT * FROM UNNEST
    (
    	$1::varchar[], $2::smallint[], $3::bigint[], $4::int[], $5::date[], $6::timestamp[],
    	$7::uuid[], $8::bigint[]
    )
    ON CONFLICT ()
    DO UPDATE SET
        the_name = EXCLUDED.the_name,
    	the_small_int = EXCLUDED.the_small_int,
    	the_large_int = EXCLUDED.the_large_int,
    	general_int = EXCLUDED.general_int,
    	the_date = EXCLUDED.the_date,
    	the_date_time = EXCLUDED.the_date_time,
    	the_uuid = EXCLUDED.the_uuid,
    	the_ulong = EXCLUDED.the_ulong
    "#,
                    &[
                        &the_name,
                        &the_small_int,
                        &the_large_int,
                        &general_int,
                        &the_date,
                        &the_date_time,
                        &the_uuid,
                        &the_ulong,
                    ],
                )
                .await;

            match &chunk_result {
                Err(err) => {
                    tracing::error!("Failed bulk_insert `keyless` chunk({chunk}) -> {err}");
                    chunk_result?;
                }
                _ => tracing::debug!(
                    "Finished inserting chunk({chunk}), size({}) in `keyless`",
                    chunk_rows.len()
                ),
            }
            chunk += 1;
            the_name.clear();
            the_small_int.clear();
            the_large_int.clear();
            general_int.clear();
            the_date.clear();
            the_date_time.clear();
            the_uuid.clear();
            the_ulong.clear();
        }
        Ok(())
    }

    /// Delete all rows of `keyless`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * _return_ - Number of rows deleted
    #[inline]
    pub async fn delete_all(client: &tokio_postgres::Client) -> Result<u64, tokio_postgres::Error> {
        Ok(client.execute("DELETE FROM keyless", &[]).await?)
    }
}

impl KeylessData {
    /// Number of fields
    pub const NUM_FIELDS: usize = 8;

    /// Names of fields
    pub const FIELD_NAMES: [&'static str; Self::NUM_FIELDS] = [
        "the_name",
        "the_small_int",
        "the_large_int",
        "general_int",
        "the_date",
        "the_date_time",
        "the_uuid",
        "the_ulong",
    ];
}

impl TableKeyless {
    /// The total number of key and value columns
    pub const COLUMN_COUNT: usize = 8;
}

/// Unit tests for `keyless`
#[cfg(test)]
pub mod unit_tests {

    /// Test type TableKeyless
    mod test_table_keyless {
        ////////////////////////////////////////////////////////////////////////////////////
        // --- functions ---
        ////////////////////////////////////////////////////////////////////////////////////
        #[serial_test::serial]
        #[tracing_test::traced_test]
        #[tokio::test]
        async fn select_all_where() {
            // α <fn test TableKeyless::select_all_where>
            todo!("Test select_all_where")
            // ω <fn test TableKeyless::select_all_where>
        }

        #[serial_test::serial]
        #[tracing_test::traced_test]
        #[tokio::test]
        async fn insert() {
            // α <fn test TableKeyless::insert>
            todo!("Test insert")
            // ω <fn test TableKeyless::insert>
        }

        #[serial_test::serial]
        #[tracing_test::traced_test]
        #[tokio::test]
        async fn bulk_insert() {
            // α <fn test TableKeyless::bulk_insert>
            todo!("Test bulk_insert")
            // ω <fn test TableKeyless::bulk_insert>
        }

        #[serial_test::serial]
        #[tracing_test::traced_test]
        #[tokio::test]
        async fn bulk_upsert() {
            // α <fn test TableKeyless::bulk_upsert>
            todo!("Test bulk_upsert")
            // ω <fn test TableKeyless::bulk_upsert>
        }

        // α <mod-def test_table_keyless>
        // ω <mod-def test_table_keyless>
    }

    // α <mod-def unit_tests>
    // ω <mod-def unit_tests>
}

// α <mod-def keyless>
// ω <mod-def keyless>
