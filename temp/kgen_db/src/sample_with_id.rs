//! Table gateway pattern implemented for SampleWithId
//!
//! > Table with auto-id

////////////////////////////////////////////////////////////////////////////////////
// --- module uses ---
////////////////////////////////////////////////////////////////////////////////////
#[allow(unused)]
use std::sync::LazyLock;
use tokio_postgres::types::ToSql;

////////////////////////////////////////////////////////////////////////////////////
// --- structs ---
////////////////////////////////////////////////////////////////////////////////////
/// Primary data fields
#[derive(Debug, Clone, Default, Eq, PartialEq, Hash)]
pub struct SampleWithIdRowData {
    /// Field for column `the_name`
    pub the_name: String,
    /// Field for column `the_boolean`
    pub the_boolean: bool,
    /// Field for column `the_small_int`
    pub the_small_int: i16,
    /// Field for column `the_large_int`
    pub the_large_int: i64,
    /// Field for column `the_big_int`
    pub the_big_int: i64,
    /// Field for column `the_date`
    pub the_date: chrono::NaiveDate,
    /// Field for column `the_general_int`
    pub the_general_int: i32,
    /// Field for column `the_date_time`
    pub the_date_time: chrono::NaiveDateTime,
    /// Field for column `the_uuid`
    pub the_uuid: uuid::Uuid,
    /// Field for column `the_ulong`
    pub the_ulong: i64,
    /// Field for column `the_json`
    pub the_json: serde_json::Value,
    /// Field for column `the_jsonb`
    pub the_jsonb: serde_json::Value,
    /// Field for column `nullable_name`
    pub nullable_name: String,
    /// Field for column `nullable_boolean`
    pub nullable_boolean: Option<bool>,
    /// Field for column `nullable_small_int`
    pub nullable_small_int: Option<i16>,
    /// Field for column `nullable_large_int`
    pub nullable_large_int: Option<i64>,
    /// Field for column `nullable_big_int`
    pub nullable_big_int: Option<i64>,
    /// Field for column `nullable_date`
    pub nullable_date: Option<chrono::NaiveDate>,
    /// Field for column `nullable_general_int`
    pub nullable_general_int: Option<i32>,
    /// Field for column `nullable_date_time`
    pub nullable_date_time: Option<chrono::NaiveDateTime>,
    /// Field for column `nullable_uuid`
    pub nullable_uuid: Option<uuid::Uuid>,
    /// Field for column `nullable_ulong`
    pub nullable_ulong: Option<i64>,
    /// Field for column `nullable_json`
    pub nullable_json: Option<serde_json::Value>,
    /// Field for column `nullable_jsonb`
    pub nullable_jsonb: serde_json::Value,
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
#[derive(Debug, Clone, Default, Eq, PartialEq, Hash)]
pub struct SampleWithIdPkey {
    /// Field for column `auto_id`
    pub auto_id: i32,
}

/// Table Gateway Support for table `sample_with_id`.
/// Table with auto-id
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
    ///   * _return_ - Selected rows
    pub async fn select_all_where<C>(
        client: &C,
        where_clause: &str,
        params: &[&(dyn ToSql + Sync)],
    ) -> Vec<SampleWithIdEntry>
    where
        C: tokio_postgres::GenericClient,
    {
        let select_where_statement = format!(
            r#"SELECT 
    auto_id, the_name, the_boolean, the_small_int, the_large_int, the_big_int,
    	the_date, the_general_int, the_date_time, the_uuid, the_ulong, the_json,
    	the_jsonb, nullable_name, nullable_boolean, nullable_small_int, nullable_large_int, nullable_big_int,
    	nullable_date, nullable_general_int, nullable_date_time, nullable_uuid, nullable_ulong, nullable_json,
    	nullable_jsonb
    FROM sample_with_id
    WHERE {where_clause}"#
        );
        let rows = match client.query(&select_where_statement, params).await {
            Ok(stmt) => stmt,
            Err(e) => {
                panic!("Error preparing statement: {e}");
            }
        };

        let mut results = Vec::<SampleWithIdEntry>::with_capacity(rows.len());

        for row in rows {
            results.push(SampleWithIdEntry {
                auto_id: row.get(0),
                data: SampleWithIdRowData {
                    the_name: row.get(1),
                    the_boolean: row.get(2),
                    the_small_int: row.get(3),
                    the_large_int: row.get(4),
                    the_big_int: row.get(5),
                    the_date: row.get(6),
                    the_general_int: row.get(7),
                    the_date_time: row.get(8),
                    the_uuid: row.get(9),
                    the_ulong: row.get(10),
                    the_json: row.get(11),
                    the_jsonb: row.get(12),
                    nullable_name: row.get(13),
                    nullable_boolean: row.get(14),
                    nullable_small_int: row.get(15),
                    nullable_large_int: row.get(16),
                    nullable_big_int: row.get(17),
                    nullable_date: row.get(18),
                    nullable_general_int: row.get(19),
                    nullable_date_time: row.get(20),
                    nullable_uuid: row.get(21),
                    nullable_ulong: row.get(22),
                    nullable_json: row.get(23),
                    nullable_jsonb: row.get(24),
                },
            });
            tracing::trace!("{:?}", results.last().unwrap());
        }
        results
    }

    /// Select rows of `sample_with_id`
    ///
    ///   * **client** - The tokio postgresql client
    ///   * _return_ - Selected rows
    #[inline]
    pub async fn select_all<C>(client: &C) -> Vec<SampleWithIdEntry>
    where
        C: tokio_postgres::GenericClient,
    {
        Self::select_all_where(client, "1=1", &[]).await
    }

    /// Insert rows of `sample_with_id` by building parameterized statement.
    /// For large insertions prefer [bulk_insert]
    ///
    ///   * **client** - The tokio postgresql client
    ///   * **rows** - Row data, consumed but returned with ids
    ///   * _return_ - Entries with corresponding _auto_id_
    pub async fn basic_insert<C>(
        client: &C,
        rows: Vec<SampleWithIdRowData>,
    ) -> Result<Vec<SampleWithIdEntry>, tokio_postgres::Error>
    where
        C: tokio_postgres::GenericClient,
    {
        use itertools::Itertools;
        let mut param_id = 0;
        let mut params: Vec<&(dyn ToSql + Sync)> =
            Vec::with_capacity(rows.len() * SampleWithIdRowData::NUM_FIELDS);
        let value_params = rows
            .iter()
            .map(|row| {
                let row_params = SampleWithIdRowData::FIELD_NAMES
                    .map(|_| {
                        param_id += 1;
                        format!("${param_id}")
                    })
                    .join(", ");

                params.push(&row.the_name);
                params.push(&row.the_boolean);
                params.push(&row.the_small_int);
                params.push(&row.the_large_int);
                params.push(&row.the_big_int);
                params.push(&row.the_date);
                params.push(&row.the_general_int);
                params.push(&row.the_date_time);
                params.push(&row.the_uuid);
                params.push(&row.the_ulong);
                params.push(&row.the_json);
                params.push(&row.the_jsonb);
                params.push(&row.nullable_name);
                params.push(&row.nullable_boolean);
                params.push(&row.nullable_small_int);
                params.push(&row.nullable_large_int);
                params.push(&row.nullable_big_int);
                params.push(&row.nullable_date);
                params.push(&row.nullable_general_int);
                params.push(&row.nullable_date_time);
                params.push(&row.nullable_uuid);
                params.push(&row.nullable_ulong);
                params.push(&row.nullable_json);
                params.push(&row.nullable_jsonb);

                format!("({row_params})")
            })
            .join(",\n");

        let insert_statement = format!(
            r#"insert into sample_with_id 
    (
    	the_name, the_boolean, the_small_int, the_large_int, the_big_int, the_date,
    	the_general_int, the_date_time, the_uuid, the_ulong, the_json, the_jsonb,
    	nullable_name, nullable_boolean, nullable_small_int, nullable_large_int, nullable_big_int, nullable_date,
    	nullable_general_int, nullable_date_time, nullable_uuid, nullable_ulong, nullable_json, nullable_jsonb
    )
    VALUES
    {value_params}
    returning auto_id
    "#
        );
        let insert_result = client.query(&insert_statement, &params).await;

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
    pub async fn bulk_insert<C>(
        client: &C,
        rows: Vec<SampleWithIdRowData>,
        chunk_size: usize,
    ) -> Result<Vec<SampleWithIdEntry>, tokio_postgres::Error>
    where
        C: tokio_postgres::GenericClient,
    {
        let mut auto_id = Vec::with_capacity(rows.len());

        let mut the_name = Vec::with_capacity(chunk_size);
        let mut the_boolean = Vec::with_capacity(chunk_size);
        let mut the_small_int = Vec::with_capacity(chunk_size);
        let mut the_large_int = Vec::with_capacity(chunk_size);
        let mut the_big_int = Vec::with_capacity(chunk_size);
        let mut the_date = Vec::with_capacity(chunk_size);
        let mut the_general_int = Vec::with_capacity(chunk_size);
        let mut the_date_time = Vec::with_capacity(chunk_size);
        let mut the_uuid = Vec::with_capacity(chunk_size);
        let mut the_ulong = Vec::with_capacity(chunk_size);
        let mut the_json = Vec::with_capacity(chunk_size);
        let mut the_jsonb = Vec::with_capacity(chunk_size);
        let mut nullable_name = Vec::with_capacity(chunk_size);
        let mut nullable_boolean = Vec::with_capacity(chunk_size);
        let mut nullable_small_int = Vec::with_capacity(chunk_size);
        let mut nullable_large_int = Vec::with_capacity(chunk_size);
        let mut nullable_big_int = Vec::with_capacity(chunk_size);
        let mut nullable_date = Vec::with_capacity(chunk_size);
        let mut nullable_general_int = Vec::with_capacity(chunk_size);
        let mut nullable_date_time = Vec::with_capacity(chunk_size);
        let mut nullable_uuid = Vec::with_capacity(chunk_size);
        let mut nullable_ulong = Vec::with_capacity(chunk_size);
        let mut nullable_json = Vec::with_capacity(chunk_size);
        let mut nullable_jsonb = Vec::with_capacity(chunk_size);

        let bulk_insert_statement = r#"insert into sample_with_id
    (
    	the_name, the_boolean, the_small_int, the_large_int, the_big_int, the_date,
    	the_general_int, the_date_time, the_uuid, the_ulong, the_json, the_jsonb,
    	nullable_name, nullable_boolean, nullable_small_int, nullable_large_int, nullable_big_int, nullable_date,
    	nullable_general_int, nullable_date_time, nullable_uuid, nullable_ulong, nullable_json, nullable_jsonb
    )
    SELECT * FROM UNNEST
    (
    	$1::varchar[], $2::boolean[], $3::smallint[], $4::bigint[], $5::bigint[], $6::date[],
    	$7::int[], $8::timestamp[], $9::uuid[], $10::bigint[], $11::json[], $12::json[],
    	$13::varchar[], $14::boolean[], $15::smallint[], $16::bigint[], $17::bigint[], $18::date[],
    	$19::int[], $20::timestamp[], $21::uuid[], $22::bigint[], $23::json[], $24::json[]
    )
    returning auto_id
    "#;
        for (chunk, chunk_rows) in rows.chunks(chunk_size).enumerate() {
            for row in chunk_rows.iter() {
                the_name.push(&row.the_name);
                the_boolean.push(row.the_boolean);
                the_small_int.push(row.the_small_int);
                the_large_int.push(row.the_large_int);
                the_big_int.push(row.the_big_int);
                the_date.push(row.the_date);
                the_general_int.push(row.the_general_int);
                the_date_time.push(row.the_date_time);
                the_uuid.push(row.the_uuid);
                the_ulong.push(row.the_ulong);
                the_json.push(&row.the_json);
                the_jsonb.push(&row.the_jsonb);
                nullable_name.push(&row.nullable_name);
                nullable_boolean.push(row.nullable_boolean);
                nullable_small_int.push(row.nullable_small_int);
                nullable_large_int.push(row.nullable_large_int);
                nullable_big_int.push(row.nullable_big_int);
                nullable_date.push(row.nullable_date);
                nullable_general_int.push(row.nullable_general_int);
                nullable_date_time.push(row.nullable_date_time);
                nullable_uuid.push(row.nullable_uuid);
                nullable_ulong.push(row.nullable_ulong);
                nullable_json.push(&row.nullable_json);
                nullable_jsonb.push(&row.nullable_jsonb);
            }

            let chunk_result = client
                .query(
                    bulk_insert_statement,
                    &[
                        &the_name,
                        &the_boolean,
                        &the_small_int,
                        &the_large_int,
                        &the_big_int,
                        &the_date,
                        &the_general_int,
                        &the_date_time,
                        &the_uuid,
                        &the_ulong,
                        &the_json,
                        &the_jsonb,
                        &nullable_name,
                        &nullable_boolean,
                        &nullable_small_int,
                        &nullable_large_int,
                        &nullable_big_int,
                        &nullable_date,
                        &nullable_general_int,
                        &nullable_date_time,
                        &nullable_uuid,
                        &nullable_ulong,
                        &nullable_json,
                        &nullable_jsonb,
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
                    chunk_result.iter().for_each(|result| {
                        auto_id.push(result.get(0));
                    });
                }
            }
            the_name.clear();
            the_boolean.clear();
            the_small_int.clear();
            the_large_int.clear();
            the_big_int.clear();
            the_date.clear();
            the_general_int.clear();
            the_date_time.clear();
            the_uuid.clear();
            the_ulong.clear();
            the_json.clear();
            the_jsonb.clear();
            nullable_name.clear();
            nullable_boolean.clear();
            nullable_small_int.clear();
            nullable_large_int.clear();
            nullable_big_int.clear();
            nullable_date.clear();
            nullable_general_int.clear();
            nullable_date_time.clear();
            nullable_uuid.clear();
            nullable_ulong.clear();
            nullable_json.clear();
            nullable_jsonb.clear();
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
    pub async fn bulk_upsert<C>(
        client: &C,
        rows: Vec<SampleWithIdRowData>,
        chunk_size: usize,
    ) -> Result<Vec<SampleWithIdEntry>, tokio_postgres::Error>
    where
        C: tokio_postgres::GenericClient,
    {
        let mut auto_id = Vec::with_capacity(rows.len());

        let mut the_name = Vec::with_capacity(chunk_size);
        let mut the_boolean = Vec::with_capacity(chunk_size);
        let mut the_small_int = Vec::with_capacity(chunk_size);
        let mut the_large_int = Vec::with_capacity(chunk_size);
        let mut the_big_int = Vec::with_capacity(chunk_size);
        let mut the_date = Vec::with_capacity(chunk_size);
        let mut the_general_int = Vec::with_capacity(chunk_size);
        let mut the_date_time = Vec::with_capacity(chunk_size);
        let mut the_uuid = Vec::with_capacity(chunk_size);
        let mut the_ulong = Vec::with_capacity(chunk_size);
        let mut the_json = Vec::with_capacity(chunk_size);
        let mut the_jsonb = Vec::with_capacity(chunk_size);
        let mut nullable_name = Vec::with_capacity(chunk_size);
        let mut nullable_boolean = Vec::with_capacity(chunk_size);
        let mut nullable_small_int = Vec::with_capacity(chunk_size);
        let mut nullable_large_int = Vec::with_capacity(chunk_size);
        let mut nullable_big_int = Vec::with_capacity(chunk_size);
        let mut nullable_date = Vec::with_capacity(chunk_size);
        let mut nullable_general_int = Vec::with_capacity(chunk_size);
        let mut nullable_date_time = Vec::with_capacity(chunk_size);
        let mut nullable_uuid = Vec::with_capacity(chunk_size);
        let mut nullable_ulong = Vec::with_capacity(chunk_size);
        let mut nullable_json = Vec::with_capacity(chunk_size);
        let mut nullable_jsonb = Vec::with_capacity(chunk_size);
        let bulk_upsert_statement = r#"insert into sample_with_id
    (
    	the_name, the_boolean, the_small_int, the_large_int, the_big_int, the_date,
    	the_general_int, the_date_time, the_uuid, the_ulong, the_json, the_jsonb,
    	nullable_name, nullable_boolean, nullable_small_int, nullable_large_int, nullable_big_int, nullable_date,
    	nullable_general_int, nullable_date_time, nullable_uuid, nullable_ulong, nullable_json, nullable_jsonb
    )
    SELECT * FROM UNNEST
    (
    	$1::varchar[], $2::boolean[], $3::smallint[], $4::bigint[], $5::bigint[], $6::date[],
    	$7::int[], $8::timestamp[], $9::uuid[], $10::bigint[], $11::json[], $12::json[],
    	$13::varchar[], $14::boolean[], $15::smallint[], $16::bigint[], $17::bigint[], $18::date[],
    	$19::int[], $20::timestamp[], $21::uuid[], $22::bigint[], $23::json[], $24::json[]
    )
    ON CONFLICT (the_name, the_small_int)
    DO UPDATE SET
        the_name = EXCLUDED.the_name,
    	the_boolean = EXCLUDED.the_boolean,
    	the_small_int = EXCLUDED.the_small_int,
    	the_large_int = EXCLUDED.the_large_int,
    	the_big_int = EXCLUDED.the_big_int,
    	the_date = EXCLUDED.the_date,
    	the_general_int = EXCLUDED.the_general_int,
    	the_date_time = EXCLUDED.the_date_time,
    	the_uuid = EXCLUDED.the_uuid,
    	the_ulong = EXCLUDED.the_ulong,
    	the_json = EXCLUDED.the_json,
    	the_jsonb = EXCLUDED.the_jsonb,
    	nullable_name = EXCLUDED.nullable_name,
    	nullable_boolean = EXCLUDED.nullable_boolean,
    	nullable_small_int = EXCLUDED.nullable_small_int,
    	nullable_large_int = EXCLUDED.nullable_large_int,
    	nullable_big_int = EXCLUDED.nullable_big_int,
    	nullable_date = EXCLUDED.nullable_date,
    	nullable_general_int = EXCLUDED.nullable_general_int,
    	nullable_date_time = EXCLUDED.nullable_date_time,
    	nullable_uuid = EXCLUDED.nullable_uuid,
    	nullable_ulong = EXCLUDED.nullable_ulong,
    	nullable_json = EXCLUDED.nullable_json,
    	nullable_jsonb = EXCLUDED.nullable_jsonb
    returning auto_id
    "#;
        for (chunk, chunk_rows) in rows.chunks(chunk_size).enumerate() {
            for row in chunk_rows.iter() {
                the_name.push(&row.the_name);
                the_boolean.push(row.the_boolean);
                the_small_int.push(row.the_small_int);
                the_large_int.push(row.the_large_int);
                the_big_int.push(row.the_big_int);
                the_date.push(row.the_date);
                the_general_int.push(row.the_general_int);
                the_date_time.push(row.the_date_time);
                the_uuid.push(row.the_uuid);
                the_ulong.push(row.the_ulong);
                the_json.push(&row.the_json);
                the_jsonb.push(&row.the_jsonb);
                nullable_name.push(&row.nullable_name);
                nullable_boolean.push(row.nullable_boolean);
                nullable_small_int.push(row.nullable_small_int);
                nullable_large_int.push(row.nullable_large_int);
                nullable_big_int.push(row.nullable_big_int);
                nullable_date.push(row.nullable_date);
                nullable_general_int.push(row.nullable_general_int);
                nullable_date_time.push(row.nullable_date_time);
                nullable_uuid.push(row.nullable_uuid);
                nullable_ulong.push(row.nullable_ulong);
                nullable_json.push(&row.nullable_json);
                nullable_jsonb.push(&row.nullable_jsonb);
            }
            let chunk_result = client
                .query(
                    bulk_upsert_statement,
                    &[
                        &the_name,
                        &the_boolean,
                        &the_small_int,
                        &the_large_int,
                        &the_big_int,
                        &the_date,
                        &the_general_int,
                        &the_date_time,
                        &the_uuid,
                        &the_ulong,
                        &the_json,
                        &the_jsonb,
                        &nullable_name,
                        &nullable_boolean,
                        &nullable_small_int,
                        &nullable_large_int,
                        &nullable_big_int,
                        &nullable_date,
                        &nullable_general_int,
                        &nullable_date_time,
                        &nullable_uuid,
                        &nullable_ulong,
                        &nullable_json,
                        &nullable_jsonb,
                    ],
                )
                .await;

            match &chunk_result {
                Err(err) => {
                    tracing::error!("Failed bulk_upsert `sample_with_id` chunk({chunk}) -> {err}");
                    chunk_result?;
                }
                Ok(chunk_result) => {
                    tracing::debug!(
                        "Finished bulk upsert of size({}) in `sample_with_id`",
                        chunk_result.len()
                    );
                    chunk_result.iter().for_each(|result| {
                        auto_id.push(result.get(0));
                    });
                }
            }
            the_name.clear();
            the_boolean.clear();
            the_small_int.clear();
            the_large_int.clear();
            the_big_int.clear();
            the_date.clear();
            the_general_int.clear();
            the_date_time.clear();
            the_uuid.clear();
            the_ulong.clear();
            the_json.clear();
            the_jsonb.clear();
            nullable_name.clear();
            nullable_boolean.clear();
            nullable_small_int.clear();
            nullable_large_int.clear();
            nullable_big_int.clear();
            nullable_date.clear();
            nullable_general_int.clear();
            nullable_date_time.clear();
            nullable_uuid.clear();
            nullable_ulong.clear();
            nullable_json.clear();
            nullable_jsonb.clear();
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
    pub async fn delete_all<C>(client: &C) -> Result<u64, tokio_postgres::Error>
    where
        C: tokio_postgres::GenericClient,
    {
        let delete_statement = "DELETE FROM sample_with_id";
        client.execute(delete_statement, &[]).await
    }
}

impl SampleWithIdRowData {
    /// Number of fields
    pub const NUM_FIELDS: usize = 24;

    /// Names of fields
    pub const FIELD_NAMES: [&'static str; Self::NUM_FIELDS] = [
        "the_name",
        "the_boolean",
        "the_small_int",
        "the_large_int",
        "the_big_int",
        "the_date",
        "the_general_int",
        "the_date_time",
        "the_uuid",
        "the_ulong",
        "the_json",
        "the_jsonb",
        "nullable_name",
        "nullable_boolean",
        "nullable_small_int",
        "nullable_large_int",
        "nullable_big_int",
        "nullable_date",
        "nullable_general_int",
        "nullable_date_time",
        "nullable_uuid",
        "nullable_ulong",
        "nullable_json",
        "nullable_jsonb",
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
    pub const COLUMN_COUNT: usize = 25;
}

// α <mod-def sample_with_id>
// ω <mod-def sample_with_id>
