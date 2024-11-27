//! Table gateway pattern implemented for SampleWithId

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
pub struct SampleWithIdRowData {
    /// Field for column `the_name`
    pub the_name: String,
    /// Field for column `the_small_int`
    pub the_small_int: i16,
    /// Field for column `the_large_int`
    pub the_large_int: i64,
    /// Field for column `the_big_int`
    pub the_big_int: i64,
    /// Field for column `general_int`
    pub general_int: i32,
    /// Field for column `the_date`
    pub the_date: chrono::NaiveDate,
    /// Field for column `the_date_time`
    pub the_date_time: chrono::NaiveDateTime,
    /// Field for column `the_uuid`
    pub the_uuid: uuid::Uuid,
}

/// All fields plus auto id for table `sample_with_id`.
#[derive(Debug, Clone)]
pub struct SampleWithIdEntry {
    /// Field for column `auto_id`
    pub auto_id: i32,
    /// The data fields
    pub data: SampleWithIdRowData,
}

/// Primary key fields for `SampleWithId`
#[derive(Debug, Clone, Default, Eq, PartialEq, Hash, Ord, PartialOrd)]
pub struct SampleWithIdPkey {
    /// Field for column `auto_id`
    pub auto_id: i32,
}

/// Table Gateway Support for table `sample_with_id`.
/// Rows
#[derive(Debug, Clone, Default)]
pub struct TableSampleWithId {}

////////////////////////////////////////////////////////////////////////////////////
// --- type impls ---
////////////////////////////////////////////////////////////////////////////////////
impl TableSampleWithId {
    /// Select rows of `sample_with_id` with provided where clause
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
    ) -> Vec<SampleWithIdEntry> {
        let statement = format!(
            r#"SELECT 
    auto_id, the_name, the_small_int, the_large_int, the_big_int, general_int,
    	the_date, the_date_time, the_uuid
    FROM sample_with_id
    WHERE {where_clause}"#
        );
        let mut results = Vec::<SampleWithIdEntry>::with_capacity(capacity);
        let rows = match client.query(&statement, params).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };

        for row in rows {
            results.push(SampleWithIdEntry {
                auto_id: row.get(0),
                data: SampleWithIdRowData {
                    the_name: row.get(1),
                    the_small_int: row.get(2),
                    the_large_int: row.get(3),
                    the_big_int: row.get(4),
                    general_int: row.get(5),
                    the_date: row.get(6),
                    the_date_time: row.get(7),
                    the_uuid: row.get(8),
                },
            });
            tracing::info!("{:?}", results.last().unwrap());
        }
        results
    }

    /// Select rows of `sample_with_id`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **capacity** - Capacity to the results
    ///   * _return_ - Selected rows
    #[inline]
    pub async fn select_all(client: &Client, capacity: usize) -> Vec<SampleWithIdEntry> {
        Self::select_all_where(&client, "1=1", &[], capacity).await
    }

    /// Insert rows of `sample_with_id` by building parameterized statement.
    /// For large insertions prefer [bulk_insert]
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Row data, consumed but returned with ids
    ///   * _return_ - Entries with corresponding _auto_id_
    pub async fn basic_insert(
        client: &Client,
        rows: Vec<SampleWithIdRowData>,
    ) -> Result<Vec<SampleWithIdEntry>, tokio_postgres::Error> {
        use itertools::Itertools;
        let mut param_id = 0;
        let mut params: Vec<&(dyn ToSql + Sync)> =
            Vec::with_capacity(rows.len() * SampleWithIdRowData::NUM_FIELDS);
        let value_params = rows
            .iter()
            .map(|row| {
                let row_params = SampleWithIdRowData::FIELD_NAMES
                    .map(|_| {
                        param_id = param_id + 1;
                        format!("${param_id}")
                    })
                    .join(", ");

                params.push(&row.the_name);
                params.push(&row.the_small_int);
                params.push(&row.the_large_int);
                params.push(&row.the_big_int);
                params.push(&row.general_int);
                params.push(&row.the_date);
                params.push(&row.the_date_time);
                params.push(&row.the_uuid);

                format!("({row_params})")
            })
            .join(",\n");

        let insert_result = client
            .query(
                &format!(
                    r#"insert into sample_with_id
    (
    	the_name, the_small_int, the_large_int, the_big_int, general_int, the_date,
    	the_date_time, the_uuid
    )
    VALUES
    {value_params}
    returning auto_id
    "#
                ),
                &params,
            )
            .await;

        match insert_result {
            Err(err) => {
                tracing::error!("Failed basic_insert `sample_with_id`");
                Err(err)
            }
            Ok(insert_result) => {
                tracing::debug!(
                    "Finished basic insert of count({}) in `sample_with_id`",
                    insert_result.len()
                );
                Ok(insert_result
                    .into_iter()
                    .zip(rows)
                    .map(|(row, data)| {
                        let auto_id = row.get(0);
                        SampleWithIdEntry { auto_id, data }
                    })
                    .collect())
            }
        }
    }

    /// Insert large batch of [SampleWithId] rows.
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Row data, consumed but returned with ids
    ///   * **chunk_size** - How to chunk the inserts
    ///   * _return_ - Entries with corresponding _auto_id_
    pub async fn bulk_insert(
        client: &Client,
        rows: Vec<SampleWithIdRowData>,
        chunk_size: usize,
    ) -> Result<Vec<SampleWithIdEntry>, tokio_postgres::Error> {
        let mut chunk = 0;
        let mut auto_id = Vec::with_capacity(rows.len());

        let mut the_name = Vec::with_capacity(chunk_size);
        let mut the_small_int = Vec::with_capacity(chunk_size);
        let mut the_large_int = Vec::with_capacity(chunk_size);
        let mut the_big_int = Vec::with_capacity(chunk_size);
        let mut general_int = Vec::with_capacity(chunk_size);
        let mut the_date = Vec::with_capacity(chunk_size);
        let mut the_date_time = Vec::with_capacity(chunk_size);
        let mut the_uuid = Vec::with_capacity(chunk_size);
        for chunk_rows in rows.chunks(chunk_size) {
            for row in chunk_rows.into_iter() {
                the_name.push(&row.the_name);
                the_small_int.push(row.the_small_int);
                the_large_int.push(row.the_large_int);
                the_big_int.push(row.the_big_int);
                general_int.push(row.general_int);
                the_date.push(row.the_date);
                the_date_time.push(row.the_date_time);
                the_uuid.push(row.the_uuid);
            }
            let chunk_result = client
                .query(
                    r#"insert into sample_with_id
    (
    	the_name, the_small_int, the_large_int, the_big_int, general_int, the_date,
    	the_date_time, the_uuid
    )
    SELECT * FROM UNNEST
    (
    	$1::varchar[], $2::smallint[], $3::bigint[], $4::bigint[], $5::int[], $6::date[],
    	$7::timestamp[], $8::uuid[]
    )
    returning auto_id
    "#,
                    &[
                        &the_name,
                        &the_small_int,
                        &the_large_int,
                        &the_big_int,
                        &general_int,
                        &the_date,
                        &the_date_time,
                        &the_uuid,
                    ],
                )
                .await;

            match &chunk_result {
                Err(err) => {
                    tracing::error!("Failed bulk_insert `sample_with_id` chunk({chunk}) -> {err}");
                    chunk_result?;
                }
                Ok(chunk_result) => {
                    tracing::debug!(
                        "Finished bulk insert of size({}) in `sample_with_id`",
                        chunk_result.len()
                    );
                    chunk_result.into_iter().for_each(|result| {
                        auto_id.push(result.get(0));
                    });
                }
            }
            chunk += 1;
            the_name.clear();
            the_small_int.clear();
            the_large_int.clear();
            the_big_int.clear();
            general_int.clear();
            the_date.clear();
            the_date_time.clear();
            the_uuid.clear();
        }

        Ok(auto_id
            .into_iter()
            .zip(rows.into_iter())
            .map(|(auto_id, data)| SampleWithIdEntry { auto_id, data })
            .collect())
    }

    /// Upsert large batch of [SampleWithId] rows.
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Row data, consumed but returned with ids
    ///   * **chunk_size** - How to chunk the inserts
    ///   * _return_ -
    pub async fn bulk_upsert(
        client: &Client,
        rows: Vec<SampleWithIdRowData>,
        chunk_size: usize,
    ) -> Result<Vec<SampleWithIdEntry>, tokio_postgres::Error> {
        let mut chunk = 0;
        let mut auto_id = Vec::with_capacity(rows.len());

        let mut the_name = Vec::with_capacity(chunk_size);
        let mut the_small_int = Vec::with_capacity(chunk_size);
        let mut the_large_int = Vec::with_capacity(chunk_size);
        let mut the_big_int = Vec::with_capacity(chunk_size);
        let mut general_int = Vec::with_capacity(chunk_size);
        let mut the_date = Vec::with_capacity(chunk_size);
        let mut the_date_time = Vec::with_capacity(chunk_size);
        let mut the_uuid = Vec::with_capacity(chunk_size);
        for chunk_rows in rows.chunks(chunk_size) {
            for row in chunk_rows.into_iter() {
                the_name.push(&row.the_name);
                the_small_int.push(row.the_small_int);
                the_large_int.push(row.the_large_int);
                the_big_int.push(row.the_big_int);
                general_int.push(row.general_int);
                the_date.push(row.the_date);
                the_date_time.push(row.the_date_time);
                the_uuid.push(row.the_uuid);
            }
            let chunk_result = client
                .query(
                    r#"insert into sample_with_id
    (
    	the_name, the_small_int, the_large_int, the_big_int, general_int, the_date,
    	the_date_time, the_uuid
    )
    SELECT * FROM UNNEST
    (
    	$1::varchar[], $2::smallint[], $3::bigint[], $4::bigint[], $5::int[], $6::date[],
    	$7::timestamp[], $8::uuid[]
    )
    ON CONFLICT (the_name, the_small_int)
    DO UPDATE SET
        the_name = EXCLUDED.the_name,
    	the_small_int = EXCLUDED.the_small_int,
    	the_large_int = EXCLUDED.the_large_int,
    	the_big_int = EXCLUDED.the_big_int,
    	general_int = EXCLUDED.general_int,
    	the_date = EXCLUDED.the_date,
    	the_date_time = EXCLUDED.the_date_time,
    	the_uuid = EXCLUDED.the_uuid
    returning auto_id
    "#,
                    &[
                        &the_name,
                        &the_small_int,
                        &the_large_int,
                        &the_big_int,
                        &general_int,
                        &the_date,
                        &the_date_time,
                        &the_uuid,
                    ],
                )
                .await;

            match &chunk_result {
                Err(err) => {
                    tracing::error!("Failed bulk_insert `sample_with_id` chunk({chunk}) -> {err}");
                    chunk_result?;
                }
                Ok(chunk_result) => {
                    tracing::debug!(
                        "Finished bulk upsert of size({}) in `sample_with_id`",
                        chunk_result.len()
                    );
                    chunk_result.into_iter().for_each(|result| {
                        auto_id.push(result.get(0));
                    });
                }
            }
            chunk += 1;
            the_name.clear();
            the_small_int.clear();
            the_large_int.clear();
            the_big_int.clear();
            general_int.clear();
            the_date.clear();
            the_date_time.clear();
            the_uuid.clear();
        }
        Ok(auto_id
            .into_iter()
            .zip(rows.into_iter())
            .map(|(auto_id, data)| SampleWithIdEntry { auto_id, data })
            .collect())
    }

    /// Delete all rows of `sample_with_id`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * _return_ - Number of rows deleted
    #[inline]
    pub async fn delete_all(client: &Client) -> Result<u64, tokio_postgres::Error> {
        client.execute("DELETE FROM sample_with_id", &[]).await
    }
}

impl SampleWithIdRowData {
    /// Number of fields
    pub const NUM_FIELDS: usize = 8;

    /// Names of fields
    pub const FIELD_NAMES: [&'static str; Self::NUM_FIELDS] = [
        "the_name",
        "the_small_int",
        "the_large_int",
        "the_big_int",
        "general_int",
        "the_date",
        "the_date_time",
        "the_uuid",
    ];
}

impl SampleWithIdPkey {
    /// Number of fields
    pub const NUM_FIELDS: usize = 1;

    /// Names of fields
    pub const FIELD_NAMES: [&'static str; Self::NUM_FIELDS] = ["auto_id"];
}

impl TableSampleWithId {
    /// The total number of key and value columns
    pub const COLUMN_COUNT: usize = 9;
}

// α <mod-def sample_with_id>
// ω <mod-def sample_with_id>
