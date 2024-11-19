//! Table gateway pattern implemented for Sample

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
pub struct SampleData {
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

/// Primary key fields for `Sample`
#[derive(Debug, Clone, Default)]
pub struct SamplePkey {
    /// Field for column `the_name`
    pub the_name: String,
    /// Field for column `the_small_int`
    pub the_small_int: i16,
}

/// Table Gateway Support for table `sample`.
/// Rows
#[derive(Debug, Clone, Default)]
pub struct TableSample {}

////////////////////////////////////////////////////////////////////////////////////
// --- type impls ---
////////////////////////////////////////////////////////////////////////////////////
impl TableSample {
    /// Select rows of `sample` with provided where clause
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
    ) -> Vec<SampleData> {
        let statement = format!(
            r#"SELECT 
    the_name, the_small_int, the_large_int, general_int, the_date, the_date_time,
    	the_uuid, the_ulong
    FROM sample
    WHERE {where_clause}"#
        );
        let mut results = Vec::<SampleData>::with_capacity(capacity);
        let rows = match client.query(&statement, params).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };

        for row in rows {
            results.push(SampleData {
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

    /// Select rows of `sample`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **capacity** - Capacity to the results
    ///   * _return_ - Selected rows
    #[inline]
    pub async fn select_all(client: &tokio_postgres::Client, capacity: usize) -> Vec<SampleData> {
        Self::select_all_where(&client, "1=1", &[], capacity).await
    }

    /// Insert rows of `sample`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Rows to insert
    pub async fn insert(client: &tokio_postgres::Client, rows: &[SampleData]) {
        todo!()
    }

    /// Insert large batch of [Sample] rows.
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Rows to insert
    ///   * **chunk_size** - How to chunk the inserts
    ///   * _return_ - TODO: Document FnReturn(bulk_insert)
    pub async fn bulk_insert(
        client: &tokio_postgres::Client,
        rows: &[SampleData],
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
                    r#"insert into sample
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
                    tracing::error!("Failed bulk_insert `sample` chunk({chunk}) -> {err}");
                    chunk_result?;
                }
                _ => tracing::debug!(
                    "Finished inserting chunk({chunk}), size({}) in `sample`",
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

    /// Upsert large batch of [Sample] rows.
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Rows to insert
    ///   * **chunk_size** - How to chunk the inserts
    ///   * _return_ -
    pub async fn bulk_upsert(
        client: &tokio_postgres::Client,
        rows: &[SampleData],
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
                    r#"insert into sample
    (
    	the_name, the_small_int, the_large_int, general_int, the_date, the_date_time,
    	the_uuid, the_ulong
    )
    SELECT * FROM UNNEST
    (
    	$1::varchar[], $2::smallint[], $3::bigint[], $4::int[], $5::date[], $6::timestamp[],
    	$7::uuid[], $8::bigint[]
    )
    ON CONFLICT (the_name, the_small_int)
    DO UPDATE SET
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
                    tracing::error!("Failed bulk_insert `sample` chunk({chunk}) -> {err}");
                    chunk_result?;
                }
                _ => tracing::debug!(
                    "Finished inserting chunk({chunk}), size({}) in `sample`",
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

    /// Delete all rows of `sample`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * _return_ - Number of rows deleted
    #[inline]
    pub async fn delete_all(client: &tokio_postgres::Client) -> Result<u64, tokio_postgres::Error> {
        Ok(client.execute("DELETE FROM sample", &[]).await?)
    }
}

impl SampleData {
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

impl SamplePkey {
    /// Number of fields
    pub const NUM_FIELDS: usize = 2;

    /// Names of fields
    pub const FIELD_NAMES: [&'static str; Self::NUM_FIELDS] = ["the_name", "the_small_int"];
}

impl TableSample {
    /// The total number of key and value columns
    pub const COLUMN_COUNT: usize = 8;
}

/// Unit tests for `sample`
#[cfg(test)]
pub mod unit_tests {

    /// Test type TableSample
    mod test_table_sample {
        ////////////////////////////////////////////////////////////////////////////////////
        // --- functions ---
        ////////////////////////////////////////////////////////////////////////////////////
        #[serial_test::serial]
        #[tracing_test::traced_test]
        #[tokio::test]
        async fn select_all_where() {
            // α <fn test TableSample::select_all_where>

            println!(
                "RUST_LOG={}",
                std::env::var("RUST_LOG").unwrap_or_else(|_| "not set".to_string())
            );

            let (client, connection) = tokio_postgres::connect(
                "host=localhost user=kgen password=kgen dbname=kgen",
                NoTls,
            )
            .await
            .unwrap();

            tokio::spawn(async move {
                if let Err(e) = connection.await {
                    eprintln!("connection error: {}", e);
                }
            });

            let rows = TableSample::select_all(&client, 10).await;
            tracing::info!("Selected all rows {rows:#?}");
            let rows = TableSample::select_all_where(&client, "general_int > 3", &[], 10).await;
            tracing::info!("Selected all rows where > 3 {rows:#?}");
            let some_general_int = 3;
            let rows = TableSample::select_all_where(
                &client,
                "general_int > $1",
                &[&some_general_int],
                10,
            )
            .await;
            tracing::info!("Selected all rows where > 3 as param {rows:#?}");
            let rows = TableSample::select_all_where(
                &client,
                "general_int BETWEEN $1 AND $1",
                &[&some_general_int],
                10,
            )
            .await;
            tracing::info!("Selected all rows between 3 and 3 {rows:#?}");
            // ω <fn test TableSample::select_all_where>
        }

        #[serial_test::serial]
        #[tracing_test::traced_test]
        #[tokio::test]
        async fn insert() {
            // α <fn test TableSample::insert>
            let (client, connection) = tokio_postgres::connect(
                "host=localhost user=kgen password=kgen dbname=kgen",
                NoTls,
            )
            .await
            .unwrap();

            tokio::spawn(async move {
                if let Err(e) = connection.await {
                    eprintln!("connection error: {}", e);
                }
            });

            let deleted = TableSample::delete_all(&client).await.unwrap();
            tracing::info!("Insert deleted {deleted} in prep for insertion");
            tracing::info!("Created {client:?}");

            TableSample::insert(&client, &sample_rows()).await;

            // ω <fn test TableSample::insert>
        }

        #[serial_test::serial]
        #[tracing_test::traced_test]
        #[tokio::test]
        async fn bulk_insert() {
            // α <fn test TableSample::bulk_insert>
            let (client, connection) = tokio_postgres::connect(
                "host=localhost user=kgen password=kgen dbname=kgen",
                NoTls,
            )
            .await
            .unwrap();

            tokio::spawn(async move {
                if let Err(e) = connection.await {
                    eprintln!("connection error: {}", e);
                }
            });

            let deleted = TableSample::delete_all(&client).await.unwrap();
            tracing::info!("Bulk insert deleted {deleted} in prep for insertion");
            tracing::info!("Created {client:?}");

            TableSample::bulk_insert(&client, &sample_rows(), 2)
                .await
                .expect("bulk_insert");
            tracing::info!("Finished bulk_insert!");

            // ω <fn test TableSample::bulk_insert>
        }

        #[serial_test::serial]
        #[tracing_test::traced_test]
        #[tokio::test]
        async fn bulk_upsert() {
            // α <fn test TableSample::bulk_upsert>
            let (client, connection) = tokio_postgres::connect(
                "host=localhost user=kgen password=kgen dbname=kgen",
                NoTls,
            )
            .await
            .unwrap();

            tokio::spawn(async move {
                if let Err(e) = connection.await {
                    eprintln!("connection error: {}", e);
                }
            });

            let deleted = TableSample::delete_all(&client).await.unwrap();
            tracing::info!("Bulk insert deleted {deleted} in prep for insertion");
            tracing::info!("Created {client:?}");

            let mut sample_rows = sample_rows();
            TableSample::bulk_insert(&client, &sample_rows, 2)
                .await
                .expect("bulk_insert");
            for row in sample_rows.iter_mut() {
                use std::ops::Add;
                row.the_name.push_str("_updated");
                row.general_int = 99999;
                row.the_small_int = 99;
                row.the_date = NaiveDate::from_ymd_opt(1929, 10, 29).unwrap();
                row.the_date_time = row.the_date.into();
                row.the_large_int = i64::MAX;
                row.the_uuid = uuid::Uuid::max();
                row.the_ulong = i64::MAX;
            }
            TableSample::bulk_upsert(&client, &sample_rows, 2)
                .await
                .expect("bulk_upsert");

            tracing::info!("Finished bulk_insert!");
            // ω <fn test TableSample::bulk_upsert>
        }

        // α <mod-def test_table_sample>
        use super::*;
        use crate::sample::*;
        use tokio_postgres::types::{Date, FromSql, ToSql};
        use tokio_postgres::NoTls;

        fn sample_rows() -> Vec<SampleData> {
            vec![
                SampleData {
                    the_name: "TEST ROW 1".to_string(),
                    the_small_int: 1i16,
                    the_large_int: 2i64,
                    general_int: 3i32,
                    the_date: chrono::NaiveDate::MAX,
                    the_date_time: chrono::NaiveDateTime::MAX,
                    the_uuid: uuid::uuid!("123e4567-e89b-12d3-a456-426655440000"),
                    the_ulong: 32i64,
                },
                SampleData {
                    the_name: "TEST ROW 2".to_string(),
                    the_small_int: 51i16,
                    the_large_int: -213i64,
                    general_int: 73i32,
                    the_date: chrono::NaiveDate::MAX,
                    the_date_time: chrono::NaiveDateTime::MAX,
                    the_uuid: uuid::uuid!("765e4321-e89b-12d3-a456-426655440000"),
                    the_ulong: 34i64,
                },
                SampleData {
                    the_name: "TEST ROW 3".to_string(),
                    the_small_int: 51i16,
                    the_large_int: -213i64,
                    general_int: 73i32,
                    the_date: chrono::NaiveDate::MAX,
                    the_date_time: chrono::NaiveDateTime::MAX,
                    the_uuid: uuid::uuid!("765e4321-e89b-12d3-a456-426655440000"),
                    the_ulong: 34i64,
                },
            ]
        }
        // ω <mod-def test_table_sample>
    }

    // α <mod-def unit_tests>
    use super::*;
    // ω <mod-def unit_tests>
}

// α <mod-def sample>
// ω <mod-def sample>
