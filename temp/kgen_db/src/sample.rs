//! Table gateway pattern implemented for Sample

////////////////////////////////////////////////////////////////////////////////////
// --- module uses ---
////////////////////////////////////////////////////////////////////////////////////
use tokio_postgres::types::ToSql;
use tokio_postgres::Client;

////////////////////////////////////////////////////////////////////////////////////
// --- structs ---
////////////////////////////////////////////////////////////////////////////////////
/// Primary data fields
#[derive(Debug, Clone, Default, Eq, PartialEq, Hash, Ord, PartialOrd)]
pub struct SampleRowData {
    /// Field for column `the_name`
    pub the_name: String,
    /// Field for column `the_small_int`
    pub the_small_int: i16,
    /// Field for column `the_large_int`
    pub the_large_int: i64,
    /// Field for column `the_big_int`
    pub the_big_int: i64,
    /// Field for column `the_date`
    pub the_date: chrono::NaiveDate,
    /// Field for column `general_int`
    pub general_int: i32,
    /// Field for column `the_date_time`
    pub the_date_time: chrono::NaiveDateTime,
    /// Field for column `the_uuid`
    pub the_uuid: uuid::Uuid,
}

/// Primary key fields for `Sample`
#[derive(Debug, Clone, Default, Eq, PartialEq, Hash, Ord, PartialOrd)]
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
        client: &Client,
        where_clause: &str,
        params: &[&(dyn ToSql + Sync)],
        capacity: usize,
    ) -> Vec<SampleRowData> {
        let statement = format!(
            r#"SELECT 
    the_name, the_small_int, the_large_int, the_big_int, the_date, general_int,
    	the_date_time, the_uuid
    FROM sample
    WHERE {where_clause}"#
        );
        let mut results = Vec::<SampleRowData>::with_capacity(capacity);
        let rows = match client.query(&statement, params).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };

        for row in rows {
            results.push(SampleRowData {
                the_name: row.get(0),
                the_small_int: row.get(1),
                the_large_int: row.get(2),
                the_big_int: row.get(3),
                the_date: row.get(4),
                general_int: row.get(5),
                the_date_time: row.get(6),
                the_uuid: row.get(7),
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
    pub async fn select_all(client: &Client, capacity: usize) -> Vec<SampleRowData> {
        Self::select_all_where(&client, "1=1", &[], capacity).await
    }

    /// Insert rows of `sample` by building parameterized statement.
    /// For large insertions prefer [bulk_insert]
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Row data to insert
    ///   * _return_ - Success or tokio_postgres::Error
    pub async fn basic_insert(
        client: &Client,
        rows: &[SampleRowData],
    ) -> Result<u64, tokio_postgres::Error> {
        use itertools::Itertools;
        let mut param_id = 0;
        let mut params: Vec<&(dyn ToSql + Sync)> =
            Vec::with_capacity(rows.len() * SampleRowData::NUM_FIELDS);
        let value_params = rows
            .iter()
            .map(|row| {
                let row_params = SampleRowData::FIELD_NAMES
                    .map(|_| {
                        param_id = param_id + 1;
                        format!("${param_id}")
                    })
                    .join(", ");

                params.push(&row.the_name);
                params.push(&row.the_small_int);
                params.push(&row.the_large_int);
                params.push(&row.the_big_int);
                params.push(&row.the_date);
                params.push(&row.general_int);
                params.push(&row.the_date_time);
                params.push(&row.the_uuid);

                format!("({row_params})")
            })
            .join(",\n");

        let insert_result = client
            .execute(
                &format!(
                    r#"insert into sample
    (
    	the_name, the_small_int, the_large_int, the_big_int, the_date, general_int,
    	the_date_time, the_uuid
    )
    VALUES
    {value_params}
    "#
                ),
                &params,
            )
            .await;

        match insert_result {
            Err(err) => {
                tracing::error!("Failed basic_insert `sample`");
                Err(err)
            }
            Ok(insert_result) => {
                tracing::debug!(
                    "Finished basic insert of count({}) in `sample`",
                    insert_result
                );
                Ok(insert_result)
            }
        }
    }

    /// Insert large batch of [Sample] rows.
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Row data to insert
    ///   * **chunk_size** - How to chunk the inserts
    ///   * _return_ - Success or tokio_postgres::Error
    pub async fn bulk_insert(
        client: &Client,
        rows: &[SampleRowData],
        chunk_size: usize,
    ) -> Result<(), tokio_postgres::Error> {
        let mut chunk = 0;
        let mut the_name = Vec::with_capacity(chunk_size);
        let mut the_small_int = Vec::with_capacity(chunk_size);
        let mut the_large_int = Vec::with_capacity(chunk_size);
        let mut the_big_int = Vec::with_capacity(chunk_size);
        let mut the_date = Vec::with_capacity(chunk_size);
        let mut general_int = Vec::with_capacity(chunk_size);
        let mut the_date_time = Vec::with_capacity(chunk_size);
        let mut the_uuid = Vec::with_capacity(chunk_size);
        for chunk_rows in rows.chunks(chunk_size) {
            for row in chunk_rows.into_iter() {
                the_name.push(&row.the_name);
                the_small_int.push(row.the_small_int);
                the_large_int.push(row.the_large_int);
                the_big_int.push(row.the_big_int);
                the_date.push(row.the_date);
                general_int.push(row.general_int);
                the_date_time.push(row.the_date_time);
                the_uuid.push(row.the_uuid);
            }
            let chunk_result = client
                .execute(
                    r#"insert into sample
    (
    	the_name, the_small_int, the_large_int, the_big_int, the_date, general_int,
    	the_date_time, the_uuid
    )
    SELECT * FROM UNNEST
    (
    	$1::varchar[], $2::smallint[], $3::bigint[], $4::bigint[], $5::date[], $6::int[],
    	$7::timestamp[], $8::uuid[]
    )
    "#,
                    &[
                        &the_name,
                        &the_small_int,
                        &the_large_int,
                        &the_big_int,
                        &the_date,
                        &general_int,
                        &the_date_time,
                        &the_uuid,
                    ],
                )
                .await;

            match &chunk_result {
                Err(err) => {
                    tracing::error!("Failed bulk_insert `sample` chunk({chunk}) -> {err}");
                    chunk_result?;
                }
                Ok(chunk_result) => {
                    tracing::debug!("Finished bulk insert of size({}) in `sample`", chunk_result);
                }
            }
            chunk += 1;
            the_name.clear();
            the_small_int.clear();
            the_large_int.clear();
            the_big_int.clear();
            the_date.clear();
            general_int.clear();
            the_date_time.clear();
            the_uuid.clear();
        }

        Ok(())
    }

    /// Upsert large batch of [Sample] rows.
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Row data to insert
    ///   * **chunk_size** - How to chunk the inserts
    ///   * _return_ -
    pub async fn bulk_upsert(
        client: &Client,
        rows: &[SampleRowData],
        chunk_size: usize,
    ) -> Result<(), tokio_postgres::Error> {
        let mut chunk = 0;
        let mut the_name = Vec::with_capacity(chunk_size);
        let mut the_small_int = Vec::with_capacity(chunk_size);
        let mut the_large_int = Vec::with_capacity(chunk_size);
        let mut the_big_int = Vec::with_capacity(chunk_size);
        let mut the_date = Vec::with_capacity(chunk_size);
        let mut general_int = Vec::with_capacity(chunk_size);
        let mut the_date_time = Vec::with_capacity(chunk_size);
        let mut the_uuid = Vec::with_capacity(chunk_size);
        for chunk_rows in rows.chunks(chunk_size) {
            for row in chunk_rows.into_iter() {
                the_name.push(&row.the_name);
                the_small_int.push(row.the_small_int);
                the_large_int.push(row.the_large_int);
                the_big_int.push(row.the_big_int);
                the_date.push(row.the_date);
                general_int.push(row.general_int);
                the_date_time.push(row.the_date_time);
                the_uuid.push(row.the_uuid);
            }
            let chunk_result = client
                .execute(
                    r#"insert into sample
    (
    	the_name, the_small_int, the_large_int, the_big_int, the_date, general_int,
    	the_date_time, the_uuid
    )
    SELECT * FROM UNNEST
    (
    	$1::varchar[], $2::smallint[], $3::bigint[], $4::bigint[], $5::date[], $6::int[],
    	$7::timestamp[], $8::uuid[]
    )
    ON CONFLICT (the_name, the_small_int)
    DO UPDATE SET
        the_large_int = EXCLUDED.the_large_int,
    	the_big_int = EXCLUDED.the_big_int,
    	the_date = EXCLUDED.the_date,
    	general_int = EXCLUDED.general_int,
    	the_date_time = EXCLUDED.the_date_time,
    	the_uuid = EXCLUDED.the_uuid
    "#,
                    &[
                        &the_name,
                        &the_small_int,
                        &the_large_int,
                        &the_big_int,
                        &the_date,
                        &general_int,
                        &the_date_time,
                        &the_uuid,
                    ],
                )
                .await;

            match &chunk_result {
                Err(err) => {
                    tracing::error!("Failed bulk_insert `sample` chunk({chunk}) -> {err}");
                    chunk_result?;
                }
                Ok(chunk_result) => {
                    tracing::debug!("Finished bulk upsert of size({}) in `sample`", chunk_result);
                }
            }
            chunk += 1;
            the_name.clear();
            the_small_int.clear();
            the_large_int.clear();
            the_big_int.clear();
            the_date.clear();
            general_int.clear();
            the_date_time.clear();
            the_uuid.clear();
        }
        Ok(())
    }

    /// Delete all rows of `sample`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * _return_ - Number of rows deleted
    #[inline]
    pub async fn delete_all(client: &Client) -> Result<u64, tokio_postgres::Error> {
        client.execute("DELETE FROM sample", &[]).await
    }
}

impl SampleRowData {
    /// Number of fields
    pub const NUM_FIELDS: usize = 8;

    /// Names of fields
    pub const FIELD_NAMES: [&'static str; Self::NUM_FIELDS] = [
        "the_name",
        "the_small_int",
        "the_large_int",
        "the_big_int",
        "the_date",
        "general_int",
        "the_date_time",
        "the_uuid",
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

// α <mod-def sample>
// ω <mod-def sample>
