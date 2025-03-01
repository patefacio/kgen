//! Tests for sample_with_id table

////////////////////////////////////////////////////////////////////////////////////
// --- module uses ---
////////////////////////////////////////////////////////////////////////////////////
use super::support::get_pool;
use super::support::MutateValue;
use kgen_db::sample_with_id::*;
use std::collections::BTreeSet;
use std::ops::Deref;
use tokio_postgres::Client;
use tokio_postgres::GenericClient;

////////////////////////////////////////////////////////////////////////////////////
// --- functions ---
////////////////////////////////////////////////////////////////////////////////////
/// Mutate fields not appearing in pkey or unique keys for test purposes with pkey.
/// Only fields not appearing in unique keys allows stability in the uniqueness of rows.
///
///   * **row_data** - Data to mutate
pub fn mutate_row_data(row_data: &mut SampleWithIdRowData) {
    row_data.the_boolean.mutate_value();
    row_data.the_large_int.mutate_value();
    row_data.the_big_int.mutate_value();
    row_data.the_date.mutate_value();
    row_data.the_general_int.mutate_value();
    row_data.the_date_time.mutate_value();
    row_data.the_uuid.mutate_value();
    row_data.the_ulong.mutate_value();
    row_data.the_json.mutate_value();
    row_data.the_jsonb.mutate_value();
    row_data.nullable_name.mutate_value();
    row_data.nullable_boolean.mutate_value();
    row_data.nullable_small_int.mutate_value();
    row_data.nullable_large_int.mutate_value();
    row_data.nullable_big_int.mutate_value();
    row_data.nullable_date.mutate_value();
    row_data.nullable_general_int.mutate_value();
    row_data.nullable_date_time.mutate_value();
    row_data.nullable_uuid.mutate_value();
    row_data.nullable_ulong.mutate_value();
    row_data.nullable_json.mutate_value();
    row_data.nullable_jsonb.mutate_value();
}

/// Select all from the database and assert they compare to [values]
///
///   * **client** - The pool connection
///   * **values** - Values to compare to selected
///   * **label** - Label for assert
pub async fn select_and_compare_assert<T>(
    client: &T,
    values: &Vec<SampleWithIdRowData>,
    label: &str,
) where
    T: GenericClient,
{
    let selected_entries = TableSampleWithId::select_all(client).await;
    let selected = entries_to_row_data(&selected_entries);
    assert_eq!(selected.len(), values.len());
    get_sample_rows_sorted(&selected)
        .iter()
        .zip(get_sample_rows_sorted(values).iter())
        .for_each(|(a, b)| {
            let matched = a == b;
            tracing::debug!(
                "{label}: {}",
                if matched {
                    format!("Match({a:?})")
                } else {
                    format!("Mismatch\n{a:?}\n---\n{b:?}")
                }
            );
            assert_eq!(true, matched);
        });
}

/// Convert entries that include both auto-id and data into the data portion
///
///   * **entries** - The entries
///   * _return_ - The data portion of the entries
#[inline]
pub fn entries_to_row_data(entries: &[SampleWithIdEntry]) -> Vec<SampleWithIdRowData> {
    entries.iter().cloned().map(|e| e.data).collect()
}

/// Get the sample rows as a set
///
///   * **rows** - The rows to stringify and sort
///   * _return_ - The samples as set
#[inline]
pub fn get_sample_rows_sorted(rows: &[SampleWithIdRowData]) -> BTreeSet<String> {
    rows.iter().cloned().map(|r| format!("{r:?}")).collect()
}

/// Get a set of sample rows for testing
///
///   * _return_ - Set of sample rows to test CRUD methods
pub fn get_sample_rows() -> Vec<SampleWithIdRowData> {
    vec![
        SampleWithIdRowData {
            the_name: "a".into(),
            the_boolean: false,
            the_small_int: -32768,
            the_large_int: -2147483648,
            the_big_int: -2147483648,
            the_date: chrono::NaiveDate::parse_from_str("2000-01-01", "%Y-%m-%d").unwrap(),
            the_general_int: -2147483648,
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-01-01T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("528f97ee-afb7-349d-ae9d-228f407501d5").unwrap(),
            the_ulong: -2147483648,
            the_json: "{ value: 1 }".into(),
            the_jsonb: "{ value: 1 }".into(),
            nullable_name: "a".into(),
            nullable_boolean: Some(false),
            nullable_small_int: Some(-32768),
            nullable_large_int: Some(-2147483648),
            nullable_big_int: Some(-2147483648),
            nullable_date: Some(
                chrono::NaiveDate::parse_from_str("2000-01-01", "%Y-%m-%d").unwrap(),
            ),
            nullable_general_int: Some(-2147483648),
            nullable_date_time: Some(
                chrono::NaiveDateTime::parse_from_str("2000-01-01T01:01", "%Y-%m-%dT%H:%M")
                    .unwrap(),
            ),
            nullable_uuid: Some(
                uuid::Uuid::parse_str("528f97ee-afb7-349d-ae9d-228f407501d5").unwrap(),
            ),
            nullable_ulong: Some(-2147483648),
            nullable_json: Some("{ value: 1 }".into()),
            nullable_jsonb: "{ value: 1 }".into(),
        },
        SampleWithIdRowData {
            the_name: "b".into(),
            the_boolean: true,
            the_small_int: -32767,
            the_large_int: -2147483647,
            the_big_int: -2147483647,
            the_date: chrono::NaiveDate::parse_from_str("2000-02-03", "%Y-%m-%d").unwrap(),
            the_general_int: -2147483647,
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-02-03T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("2d41f1a3-e690-3d91-8a3e-cce82beaf5a5").unwrap(),
            the_ulong: -2147483647,
            the_json: "{ value: 2 }".into(),
            the_jsonb: "{ value: 2 }".into(),
            nullable_name: "b".into(),
            nullable_boolean: Some(true),
            nullable_small_int: Some(-32767),
            nullable_large_int: Some(-2147483647),
            nullable_big_int: Some(-2147483647),
            nullable_date: Some(
                chrono::NaiveDate::parse_from_str("2000-02-03", "%Y-%m-%d").unwrap(),
            ),
            nullable_general_int: Some(-2147483647),
            nullable_date_time: Some(
                chrono::NaiveDateTime::parse_from_str("2000-02-03T01:01", "%Y-%m-%dT%H:%M")
                    .unwrap(),
            ),
            nullable_uuid: Some(
                uuid::Uuid::parse_str("2d41f1a3-e690-3d91-8a3e-cce82beaf5a5").unwrap(),
            ),
            nullable_ulong: Some(-2147483647),
            nullable_json: Some("{ value: 2 }".into()),
            nullable_jsonb: "{ value: 2 }".into(),
        },
        SampleWithIdRowData {
            the_name: "c".into(),
            the_boolean: false,
            the_small_int: -32766,
            the_large_int: -2147483646,
            the_big_int: -2147483646,
            the_date: chrono::NaiveDate::parse_from_str("2000-03-05", "%Y-%m-%d").unwrap(),
            the_general_int: -2147483646,
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-03-05T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("01ff6bbb-b780-3928-addf-5f189dc96802").unwrap(),
            the_ulong: -2147483646,
            the_json: "{ value: 3 }".into(),
            the_jsonb: "{ value: 3 }".into(),
            nullable_name: "c".into(),
            nullable_boolean: Some(false),
            nullable_small_int: Some(-32766),
            nullable_large_int: Some(-2147483646),
            nullable_big_int: Some(-2147483646),
            nullable_date: Some(
                chrono::NaiveDate::parse_from_str("2000-03-05", "%Y-%m-%d").unwrap(),
            ),
            nullable_general_int: Some(-2147483646),
            nullable_date_time: Some(
                chrono::NaiveDateTime::parse_from_str("2000-03-05T01:01", "%Y-%m-%dT%H:%M")
                    .unwrap(),
            ),
            nullable_uuid: Some(
                uuid::Uuid::parse_str("01ff6bbb-b780-3928-addf-5f189dc96802").unwrap(),
            ),
            nullable_ulong: Some(-2147483646),
            nullable_json: Some("{ value: 3 }".into()),
            nullable_jsonb: "{ value: 3 }".into(),
        },
        SampleWithIdRowData {
            the_name: "d".into(),
            the_boolean: true,
            the_small_int: -32765,
            the_large_int: -2147483645,
            the_big_int: -2147483645,
            the_date: chrono::NaiveDate::parse_from_str("2000-04-07", "%Y-%m-%d").unwrap(),
            the_general_int: -2147483645,
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-04-07T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("7fa8058e-840e-362e-a8e3-9d1b75f39fe8").unwrap(),
            the_ulong: -2147483645,
            the_json: "{ value: 4 }".into(),
            the_jsonb: "{ value: 4 }".into(),
            nullable_name: "d".into(),
            nullable_boolean: Some(true),
            nullable_small_int: Some(-32765),
            nullable_large_int: Some(-2147483645),
            nullable_big_int: Some(-2147483645),
            nullable_date: Some(
                chrono::NaiveDate::parse_from_str("2000-04-07", "%Y-%m-%d").unwrap(),
            ),
            nullable_general_int: Some(-2147483645),
            nullable_date_time: Some(
                chrono::NaiveDateTime::parse_from_str("2000-04-07T01:01", "%Y-%m-%dT%H:%M")
                    .unwrap(),
            ),
            nullable_uuid: Some(
                uuid::Uuid::parse_str("7fa8058e-840e-362e-a8e3-9d1b75f39fe8").unwrap(),
            ),
            nullable_ulong: Some(-2147483645),
            nullable_json: Some("{ value: 4 }".into()),
            nullable_jsonb: "{ value: 4 }".into(),
        },
        SampleWithIdRowData {
            the_name: "e".into(),
            the_boolean: false,
            the_small_int: -32764,
            the_large_int: -2147483644,
            the_big_int: -2147483644,
            the_date: chrono::NaiveDate::parse_from_str("2000-05-09", "%Y-%m-%d").unwrap(),
            the_general_int: -2147483644,
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-05-09T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("0edc1aeb-3200-3d26-b2be-77c5039aecf3").unwrap(),
            the_ulong: -2147483644,
            the_json: "{ value: 5 }".into(),
            the_jsonb: "{ value: 5 }".into(),
            nullable_name: "e".into(),
            nullable_boolean: Some(false),
            nullable_small_int: Some(-32764),
            nullable_large_int: Some(-2147483644),
            nullable_big_int: Some(-2147483644),
            nullable_date: Some(
                chrono::NaiveDate::parse_from_str("2000-05-09", "%Y-%m-%d").unwrap(),
            ),
            nullable_general_int: Some(-2147483644),
            nullable_date_time: Some(
                chrono::NaiveDateTime::parse_from_str("2000-05-09T01:01", "%Y-%m-%dT%H:%M")
                    .unwrap(),
            ),
            nullable_uuid: Some(
                uuid::Uuid::parse_str("0edc1aeb-3200-3d26-b2be-77c5039aecf3").unwrap(),
            ),
            nullable_ulong: Some(-2147483644),
            nullable_json: Some("{ value: 5 }".into()),
            nullable_jsonb: "{ value: 5 }".into(),
        },
        SampleWithIdRowData {
            the_name: "f".into(),
            the_boolean: true,
            the_small_int: -32763,
            the_large_int: -2147483643,
            the_big_int: -2147483643,
            the_date: chrono::NaiveDate::parse_from_str("2000-06-11", "%Y-%m-%d").unwrap(),
            the_general_int: -2147483643,
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-06-11T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("9d6a2a8e-4852-33e4-ade0-7aaadb41066d").unwrap(),
            the_ulong: -2147483643,
            the_json: "{ value: 6 }".into(),
            the_jsonb: "{ value: 6 }".into(),
            nullable_name: "f".into(),
            nullable_boolean: Some(true),
            nullable_small_int: Some(-32763),
            nullable_large_int: Some(-2147483643),
            nullable_big_int: Some(-2147483643),
            nullable_date: Some(
                chrono::NaiveDate::parse_from_str("2000-06-11", "%Y-%m-%d").unwrap(),
            ),
            nullable_general_int: Some(-2147483643),
            nullable_date_time: Some(
                chrono::NaiveDateTime::parse_from_str("2000-06-11T01:01", "%Y-%m-%dT%H:%M")
                    .unwrap(),
            ),
            nullable_uuid: Some(
                uuid::Uuid::parse_str("9d6a2a8e-4852-33e4-ade0-7aaadb41066d").unwrap(),
            ),
            nullable_ulong: Some(-2147483643),
            nullable_json: Some("{ value: 6 }".into()),
            nullable_jsonb: "{ value: 6 }".into(),
        },
        SampleWithIdRowData {
            the_name: "g".into(),
            the_boolean: false,
            the_small_int: -32762,
            the_large_int: -2147483642,
            the_big_int: -2147483642,
            the_date: chrono::NaiveDate::parse_from_str("2000-07-13", "%Y-%m-%d").unwrap(),
            the_general_int: -2147483642,
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-07-13T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("f89673f7-afd4-3121-b58d-c3b683a88e4d").unwrap(),
            the_ulong: -2147483642,
            the_json: "{ value: 7 }".into(),
            the_jsonb: "{ value: 7 }".into(),
            nullable_name: "g".into(),
            nullable_boolean: Some(false),
            nullable_small_int: Some(-32762),
            nullable_large_int: Some(-2147483642),
            nullable_big_int: Some(-2147483642),
            nullable_date: Some(
                chrono::NaiveDate::parse_from_str("2000-07-13", "%Y-%m-%d").unwrap(),
            ),
            nullable_general_int: Some(-2147483642),
            nullable_date_time: Some(
                chrono::NaiveDateTime::parse_from_str("2000-07-13T01:01", "%Y-%m-%dT%H:%M")
                    .unwrap(),
            ),
            nullable_uuid: Some(
                uuid::Uuid::parse_str("f89673f7-afd4-3121-b58d-c3b683a88e4d").unwrap(),
            ),
            nullable_ulong: Some(-2147483642),
            nullable_json: Some("{ value: 7 }".into()),
            nullable_jsonb: "{ value: 7 }".into(),
        },
        SampleWithIdRowData {
            the_name: "h".into(),
            the_boolean: true,
            the_small_int: -32761,
            the_large_int: -2147483641,
            the_big_int: -2147483641,
            the_date: chrono::NaiveDate::parse_from_str("2000-08-15", "%Y-%m-%d").unwrap(),
            the_general_int: -2147483641,
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-08-15T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("9f56050f-c322-3efa-9e1f-96f4d3217930").unwrap(),
            the_ulong: -2147483641,
            the_json: "{ value: 8 }".into(),
            the_jsonb: "{ value: 8 }".into(),
            nullable_name: "h".into(),
            nullable_boolean: Some(true),
            nullable_small_int: Some(-32761),
            nullable_large_int: Some(-2147483641),
            nullable_big_int: Some(-2147483641),
            nullable_date: Some(
                chrono::NaiveDate::parse_from_str("2000-08-15", "%Y-%m-%d").unwrap(),
            ),
            nullable_general_int: Some(-2147483641),
            nullable_date_time: Some(
                chrono::NaiveDateTime::parse_from_str("2000-08-15T01:01", "%Y-%m-%dT%H:%M")
                    .unwrap(),
            ),
            nullable_uuid: Some(
                uuid::Uuid::parse_str("9f56050f-c322-3efa-9e1f-96f4d3217930").unwrap(),
            ),
            nullable_ulong: Some(-2147483641),
            nullable_json: Some("{ value: 8 }".into()),
            nullable_jsonb: "{ value: 8 }".into(),
        },
        SampleWithIdRowData {
            the_name: "i".into(),
            the_boolean: false,
            the_small_int: -32760,
            the_large_int: -2147483640,
            the_big_int: -2147483640,
            the_date: chrono::NaiveDate::parse_from_str("2000-09-17", "%Y-%m-%d").unwrap(),
            the_general_int: -2147483640,
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-09-17T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("caa9705c-21b1-387a-afd8-de69e0698cb7").unwrap(),
            the_ulong: -2147483640,
            the_json: "{ value: 9 }".into(),
            the_jsonb: "{ value: 9 }".into(),
            nullable_name: "i".into(),
            nullable_boolean: Some(false),
            nullable_small_int: Some(-32760),
            nullable_large_int: Some(-2147483640),
            nullable_big_int: Some(-2147483640),
            nullable_date: Some(
                chrono::NaiveDate::parse_from_str("2000-09-17", "%Y-%m-%d").unwrap(),
            ),
            nullable_general_int: Some(-2147483640),
            nullable_date_time: Some(
                chrono::NaiveDateTime::parse_from_str("2000-09-17T01:01", "%Y-%m-%dT%H:%M")
                    .unwrap(),
            ),
            nullable_uuid: Some(
                uuid::Uuid::parse_str("caa9705c-21b1-387a-afd8-de69e0698cb7").unwrap(),
            ),
            nullable_ulong: Some(-2147483640),
            nullable_json: Some("{ value: 9 }".into()),
            nullable_jsonb: "{ value: 9 }".into(),
        },
        SampleWithIdRowData {
            the_name: "j".into(),
            the_boolean: true,
            the_small_int: -32759,
            the_large_int: -2147483639,
            the_big_int: -2147483639,
            the_date: chrono::NaiveDate::parse_from_str("2000-10-19", "%Y-%m-%d").unwrap(),
            the_general_int: -2147483639,
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-10-19T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("0455e422-6d84-35e3-9db8-1eb8c06af841").unwrap(),
            the_ulong: -2147483639,
            the_json: "{ value: 10 }".into(),
            the_jsonb: "{ value: 10 }".into(),
            nullable_name: "j".into(),
            nullable_boolean: Some(true),
            nullable_small_int: Some(-32759),
            nullable_large_int: Some(-2147483639),
            nullable_big_int: Some(-2147483639),
            nullable_date: Some(
                chrono::NaiveDate::parse_from_str("2000-10-19", "%Y-%m-%d").unwrap(),
            ),
            nullable_general_int: Some(-2147483639),
            nullable_date_time: Some(
                chrono::NaiveDateTime::parse_from_str("2000-10-19T01:01", "%Y-%m-%dT%H:%M")
                    .unwrap(),
            ),
            nullable_uuid: Some(
                uuid::Uuid::parse_str("0455e422-6d84-35e3-9db8-1eb8c06af841").unwrap(),
            ),
            nullable_ulong: Some(-2147483639),
            nullable_json: Some("{ value: 10 }".into()),
            nullable_jsonb: "{ value: 10 }".into(),
        },
    ]
}

/// Test by delete, bulk insert, select, bulk upsert, then delete for sample_with_id
#[tracing_test::traced_test]
#[serial_test::serial]
#[tokio::test]
pub async fn test_crud() {
    let pool = get_pool().await;
    let resource = pool.get().await.unwrap();
    let client = resource.client();
    // First delete all, assuming it worked
    let deleted = TableSampleWithId::delete_all(client).await.unwrap();
    tracing::info!("Initialize phase deleted {deleted}");

    /*
      Validate that delete work by selecting back an empty set
    */
    {
        assert_eq!(0, TableSampleWithId::select_all(client).await.len());
    }
    let mut samples = get_sample_rows();

    /*
      Test the basic insert functionality
    */
    {
        let inserted = TableSampleWithId::basic_insert(client, samples.clone())
            .await
            .unwrap();

        tracing::debug!("Inserted with `basic_insert` -> {inserted:?}");

        /*
          Select back out the inserted data and compare to samples
        */
        {
            select_and_compare_assert(
                client,
                &inserted.into_iter().map(|r| r.data).collect(),
                "Basic Ins Cmp",
            )
            .await;
        }
        let deleted = TableSampleWithId::delete_all(client).await.unwrap();
        tracing::info!("Basic insert phase deleted {deleted}");
        assert_eq!(samples.len(), deleted as usize);
    }

    /*
      Test the bulk insert functionality
    */
    {
        let inserted = TableSampleWithId::bulk_insert(client, samples.clone(), 4)
            .await
            .unwrap();
        tracing::debug!("Inserted with `bulk_insert` -> {inserted:?}");
        /*
          Select back out the inserted data and compare to samples
        */
        select_and_compare_assert(
            client,
            &inserted.into_iter().map(|r| r.data).collect(),
            "Blk Ins Cmp",
        )
        .await;
    }

    /*
      Mutate the data, and bulk upsert.
    */
    {
        samples.iter_mut().for_each(|data| mutate_row_data(data));
        tracing::debug!("Mutated Samples: {samples:?}");
        let upserted = TableSampleWithId::bulk_upsert(client, samples.clone(), 4)
            .await
            .unwrap();
        tracing::debug!("Inserted with `bulk_upsert` -> {upserted:?}");
        select_and_compare_assert(client, &samples.iter().cloned().collect(), "Blk Upsert Cmp")
            .await;
    }

    /*
      Deleted all entries
    */
    {
        let deleted = TableSampleWithId::delete_all(client).await.unwrap();
        tracing::info!("Deleted all {deleted} TableSampleWithId entries");
        assert_eq!(deleted as usize, samples.len());
        let selected = TableSampleWithId::select_all(client).await;
        assert_eq!(0, selected.len());
    }
}

// α <mod-def test_sample_with_id>
// ω <mod-def test_sample_with_id>
