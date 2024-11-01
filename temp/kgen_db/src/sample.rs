//! Table gateway pattern implemented for Sample

////////////////////////////////////////////////////////////////////////////////////
// --- module uses ---
////////////////////////////////////////////////////////////////////////////////////
use chrono::{NaiveDate, NaiveDateTime};
use tokio_postgres::types::{Date, FromSql, ToSql};

////////////////////////////////////////////////////////////////////////////////////
// --- type aliases ---
////////////////////////////////////////////////////////////////////////////////////
/// Rows are composed of the primary key and the value fields
pub type SampleRow = (SamplePkey, SampleValues);

////////////////////////////////////////////////////////////////////////////////////
// --- constants ---
////////////////////////////////////////////////////////////////////////////////////
/// Column names
pub const SAMPLE_COLUMN_SET: &'static str =
    "(id, the_name, the_small_int, the_large_int, general_int, the_date,
	the_date_time, the_uuid, the_ulong)";
/// Unnest column expressions
pub const UNNESTED_COLUMN_EXPRESSION: &'static str =
    "($1::int[], $2::varchar[], $3::smallint[], $4::bigint[], $5::int[], $6::date[],
	$7::timestamp[], $8::uuid[], $9::bigint[])";
/// Total number of columns, primary key columns and non-key columns
pub const SAMPLE_PKEY_COLUMN_COUNT: usize = 1;
/// Total number of columns in the primary key
pub const SAMPLE_VALUE_COLUMN_COUNT: usize = 8;
/// Total number of columns
pub const SAMPLE_COLUMN_COUNT: usize = 9;

////////////////////////////////////////////////////////////////////////////////////
// --- structs ---
////////////////////////////////////////////////////////////////////////////////////
/// Primary key fields for `Sample`
#[derive(Debug, Clone, Default)]
pub struct SamplePkey {
    /// Field for column `id`
    pub id: i32,
}

/// Value fields for `Sample`
#[derive(Debug, Clone, Default)]
pub struct SampleValues {
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

/// Table Gateway Support for table `sample`.
/// Rows
#[derive(Debug, Clone, Default)]
pub struct TableSample {}

////////////////////////////////////////////////////////////////////////////////////
// --- type impls ---
////////////////////////////////////////////////////////////////////////////////////
impl TableSample {
    /// Insert rows of `sample`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Rows to insert
    pub async fn insert(client: &tokio_postgres::Client, rows: &[SampleRow]) {
        use itertools::Itertools;
        let values_placeholder = rows
            .into_iter()
            .enumerate()
            .map(|(i, row)| {
                let start = 1 + i * SAMPLE_COLUMN_COUNT;
                format!(
                    "({})",
                    (start..start + SAMPLE_COLUMN_COUNT)
                        .map(|param_index| format!("${param_index}"))
                        .join(", ")
                )
            })
            .join(",\n\t");

        let statement = format!(
            r#"insert into sample
    {SAMPLE_COLUMN_SET}
    values 
    {values_placeholder}
    returning id
    "#
        );
        tracing::info!("SQL ->```\n{statement}\n```");

        let mut params =
            Vec::<&(dyn ToSql + Sync)>::with_capacity(rows.len() * SAMPLE_COLUMN_COUNT);
        for row in rows {
            params.push(&row.0.id);
            params.push(&row.1.the_name);
            params.push(&row.1.the_small_int);
            params.push(&row.1.the_large_int);
            params.push(&row.1.general_int);
            params.push(&row.1.the_date);
            params.push(&row.1.the_date_time);
            params.push(&row.1.the_uuid);
            params.push(&row.1.the_ulong);
        }

        let results = match client.query(&statement, &params[..]).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };

        results
            .iter()
            .for_each(|row| tracing::info!("Row id -> {:?}", row.get::<usize, i32>(0)));
    }

    /// Insert large batch of [Sample] rows.
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Rows to insert
    ///   * **chunk_size** - How to chunk the inserts
    ///   * _return_ - TODO: Document FnReturn(bulk_insert)
    pub async fn bulk_insert(
        client: &tokio_postgres::Client,
        rows: &[SampleRow],
        chunk_size: usize,
    ) -> Result<(), tokio_postgres::Error> {
        let insert_statement = r#"insert into sample
    (id, the_name, the_small_int, the_large_int, general_int, the_date,
    	the_date_time, the_uuid, the_ulong)
    SELECT * FROM UNNEST
    ($1::int[], $2::varchar[], $3::smallint[], $4::bigint[], $5::int[], $6::date[],
    	$7::timestamp[], $8::uuid[], $9::bigint[])
    "#;
        let mut chunk = 0;
        let mut id = Vec::with_capacity(chunk_size);
        let mut the_name = Vec::with_capacity(chunk_size);
        let mut the_small_int = Vec::with_capacity(chunk_size);
        let mut the_large_int = Vec::with_capacity(chunk_size);
        let mut general_int = Vec::with_capacity(chunk_size);
        let mut the_date = Vec::with_capacity(chunk_size);
        let mut the_date_time = Vec::with_capacity(chunk_size);
        let mut the_uuid = Vec::with_capacity(chunk_size);
        let mut the_ulong = Vec::with_capacity(chunk_size);
        for chunk_rows in rows.chunks(chunk_size) {
            for (key, value) in chunk_rows.into_iter() {
                id.push(key.id);
                the_name.push(&value.the_name);
                the_small_int.push(value.the_small_int);
                the_large_int.push(value.the_large_int);
                general_int.push(value.general_int);
                the_date.push(value.the_date);
                the_date_time.push(value.the_date_time);
                the_uuid.push(value.the_uuid);
                the_ulong.push(value.the_ulong);
            }
            let chunk_result = client
                .execute(
                    insert_statement,
                    &[
                        &id,
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
            id.clear();
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
    pub fn bulk_upsert(client: &tokio_postgres::Client, rows: &[SampleRow], chunk_size: usize) {
        // α <fn TableSample::bulk_upsert>
        todo!("Implement `bulk_upsert`")
        // ω <fn TableSample::bulk_upsert>
    }

    /// Select rows of `sample`
    ///
    ///   * **client** - The tokio postgresql client
    pub async fn select(client: &tokio_postgres::Client) {
        //HERE
    }

    /// Update rows of `sample`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **s_clause** - clause for SET statement
    ///   * **w_clause** - clause for WHERE statement
    pub async fn update(client: &tokio_postgres::Client, s_clause: String, w_clause: String) {
        let mut statement = "update sample SET ".to_string();

        if s_clause != "" {
            statement = statement + &s_clause + " WHERE ";
        } else {
        }

        if w_clause != "" {
            statement = statement + &w_clause + " RETURNING *";
        } else {
        }

        println!("{}", statement);

        let mut params = Vec::<&(dyn ToSql + Sync)>::with_capacity(0);

        let results = match client.query(&statement, &params[..]).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };

        results
            .iter()
            .for_each(|row| tracing::info!("updated row id -> {:?}", row.get::<usize, i32>(0)));
    }

    /// Delete rows of `sample`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **clause** - full clause, skips input vectors
    ///   * **cols** - columns list for clause
    ///   * **ops** - operator list for clause
    ///   * **conds** - conditions list for clause
    pub async fn delete(
        client: &tokio_postgres::Client,
        clause: String,
        cols: Vec<&str>,
        ops: Vec<&str>,
        conds: Vec<&str>,
    ) {
        let col_num = 9;
        assert!(cols.len() == ops.len() && ops.len() == conds.len());
        assert!(cols.len() <= col_num);

        let mut statement = "delete from sample where (".to_string();

        if clause != "" {
            statement = statement + &clause + ") RETURNING *";
        } else {
            for i in 0..cols.len() - 1 {
                statement = statement + cols[i] + " " + ops[i] + " " + conds[i] + " OR ";
            }
            statement = statement
                + cols[cols.len() - 1]
                + " "
                + ops[cols.len() - 1]
                + " "
                + conds[cols.len() - 1]
                + ") RETURNING *";
        }

        let mut params = Vec::<&(dyn ToSql + Sync)>::with_capacity(0);

        let results = match client.query(&statement, &params[..]).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };

        results
            .iter()
            .for_each(|row| tracing::info!("deleted row id -> {:?}", row.get::<usize, i32>(0)));
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

            TableSample::bulk_insert(&client, &sample_rows(), 2).await.expect("bulk_insert");
            tracing::info!("Finished bulk_insert!");

            // ω <fn test TableSample::bulk_insert>
        }

        #[serial_test::serial]
        #[tracing_test::traced_test]
        fn bulk_upsert() {
            // α <fn test TableSample::bulk_upsert>
            todo!("Test bulk_upsert")
            // ω <fn test TableSample::bulk_upsert>
        }

        #[serial_test::serial]
        #[tracing_test::traced_test]
        #[tokio::test]
        async fn select() {
            // α <fn test TableSample::select>
            // TESTING GITHUB ACTIONS
            use crate::sample::*;
            use crate::SampleRow;
            use tokio_postgres::types::{Date, FromSql, ToSql};
            use tokio_postgres::NoTls;

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

            tracing::info!("Created {client:?}");

            let mut input_cols = vec!["id", "the_name", "general_int"];
            let mut input_operator = vec!["=", "=", "<"];
            let mut input_condition = vec!["2", "\'TEST ROW 3\'", "5"];

            let clause = "id >= 2 OR the_name = 'TEST ROW 3' OR general_int = 3".to_string();

            //TableSample::select(client, clause, input_cols, input_operator, input_condition).await;
            // ω <fn test TableSample::select>
        }

        #[serial_test::serial]
        #[tracing_test::traced_test]
        #[tokio::test]
        async fn update() {
            // α <fn test TableSample::update>
            use crate::sample::*;
            use crate::SampleRow;
            use tokio_postgres::types::{Date, FromSql, ToSql};
            use tokio_postgres::NoTls;

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

            tracing::info!("Created {client:?}");

            let where_clause = "the_name LIKE 'TEST ROW%'".to_string();
            let set_clause = "the_name = 'UPDATED ROW 3'".to_string();

            TableSample::update(&client, set_clause, where_clause).await;
            // ω <fn test TableSample::update>
        }

        #[serial_test::serial]
        #[tracing_test::traced_test]
        #[tokio::test]
        async fn delete() {
            // α <fn test TableSample::delete>
            use crate::sample::*;
            use crate::SampleRow;
            use tokio_postgres::types::{Date, FromSql, ToSql};
            use tokio_postgres::NoTls;

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

            tracing::info!("Created {client:?}");

            let mut input_cols = vec!["id", "the_name", "general_int"];
            let mut input_operator = vec!["=", "=", "<"];
            let mut input_condition = vec!["2", "\'TEST ROW 3\'", "5"];

            let clause = "id = 2 OR the_name LIKE '%ROW 3' OR general_int = 3".to_string();

            TableSample::delete(&client, clause, input_cols, input_operator, input_condition).await;

            // ω <fn test TableSample::delete>
        }

        // α <mod-def test_table_sample>
        use crate::sample::*;
        use crate::SampleRow;
        use tokio_postgres::types::{Date, FromSql, ToSql};
        use tokio_postgres::NoTls;

        fn sample_rows() -> Vec<SampleRow> {
            let row_1: SampleRow = (
                SamplePkey { id: 1 },
                SampleValues {
                    the_name: "TEST ROW 1".to_string(),
                    the_small_int: 1i16,
                    the_large_int: 2i64,
                    general_int: 3i32,
                    the_date: chrono::NaiveDate::MAX,
                    the_date_time: chrono::NaiveDateTime::MAX,
                    the_uuid: uuid::uuid!("123e4567-e89b-12d3-a456-426655440000"),
                    the_ulong: 32i64,
                },
            );
            let row_2: SampleRow = (
                SamplePkey { id: 2 },
                SampleValues {
                    the_name: "TEST ROW 2".to_string(),
                    the_small_int: 51i16,
                    the_large_int: -213i64,
                    general_int: 73i32,
                    the_date: chrono::NaiveDate::MAX,
                    the_date_time: chrono::NaiveDateTime::MAX,
                    the_uuid: uuid::uuid!("765e4321-e89b-12d3-a456-426655440000"),
                    the_ulong: 34i64,
                },
            );
            let row_3: SampleRow = (
                SamplePkey { id: 3 },
                SampleValues {
                    the_name: "TEST ROW 3".to_string(),
                    the_small_int: 51i16,
                    the_large_int: -213i64,
                    general_int: 73i32,
                    the_date: chrono::NaiveDate::MAX,
                    the_date_time: chrono::NaiveDateTime::MAX,
                    the_uuid: uuid::uuid!("765e4321-e89b-12d3-a456-426655440000"),
                    the_ulong: 34i64,
                },
            );

            vec![row_1, row_2, row_3]
        }
        // ω <mod-def test_table_sample>
    }

    // α <mod-def unit_tests>
    // ω <mod-def unit_tests>
}

// α <mod-def sample>
// ω <mod-def sample>
