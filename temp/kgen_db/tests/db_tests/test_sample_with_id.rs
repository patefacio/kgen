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

////////////////////////////////////////////////////////////////////////////////////
// --- functions ---
////////////////////////////////////////////////////////////////////////////////////
/// Mutate fields not appearing in pkey or unique keys for test purposes with pkey.
/// Only fields not appearing in unique keys allows stability in the uniqueness of rows.
///
///   * **row_data** - Data to mutate
pub fn mutate_row_data(row_data: &mut SampleWithIdRowData) {
    row_data.the_large_int.mutate_value();
    row_data.the_big_int.mutate_value();
    row_data.general_int.mutate_value();
    row_data.the_date.mutate_value();
    row_data.the_date_time.mutate_value();
    row_data.the_uuid.mutate_value();
}

/// Select all from the database and assert they compare to [values]
///
///   * **pool_conn** - The pool connection
///   * **values** - Values to compare to selected
///   * **label** - Label for assert
pub async fn select_and_compare_assert<T>(
    pool_conn: &T,
    values: &BTreeSet<SampleWithIdRowData>,
    label: &str,
) where
    T: Deref<Target = Client>,
{
    let selected_entries = TableSampleWithId::select_all(&pool_conn, 4).await;
    let selected = entries_to_row_data(&selected_entries)
        .into_iter()
        .collect::<BTreeSet<_>>();
    assert_eq!(selected.len(), values.len());
    selected.iter().zip(values.iter()).for_each(|(a, b)| {
        let matched = a == b;
        tracing::debug!(
            "{label}: {}",
            if matched {
                format!("Match({a:?})")
            } else {
                "Mismatch".to_string()
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
///   * _return_ - The samples as set
#[inline]
pub fn get_sample_rows_set() -> BTreeSet<SampleWithIdRowData> {
    get_sample_rows().iter().cloned().collect()
}

/// Get a set of sample rows for testing
///
///   * _return_ - Set of sample rows to test CRUD methods
pub fn get_sample_rows() -> Vec<SampleWithIdRowData> {
    vec![
        SampleWithIdRowData {
            the_name: "a".into(),
            the_small_int: -32768,
            the_large_int: -2147483648,
            the_big_int: -2147483648,
            general_int: -2147483648,
            the_date: chrono::NaiveDate::parse_from_str("2000-01-01", "%Y-%m-%d").unwrap(),
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-01-01T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("528f97ee-afb7-349d-ae9d-228f407501d5").unwrap(),
        },
        SampleWithIdRowData {
            the_name: "b".into(),
            the_small_int: -32767,
            the_large_int: -2147483647,
            the_big_int: -2147483647,
            general_int: -2147483647,
            the_date: chrono::NaiveDate::parse_from_str("2000-02-03", "%Y-%m-%d").unwrap(),
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-02-03T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("2d41f1a3-e690-3d91-8a3e-cce82beaf5a5").unwrap(),
        },
        SampleWithIdRowData {
            the_name: "c".into(),
            the_small_int: -32766,
            the_large_int: -2147483646,
            the_big_int: -2147483646,
            general_int: -2147483646,
            the_date: chrono::NaiveDate::parse_from_str("2000-03-05", "%Y-%m-%d").unwrap(),
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-03-05T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("01ff6bbb-b780-3928-addf-5f189dc96802").unwrap(),
        },
        SampleWithIdRowData {
            the_name: "d".into(),
            the_small_int: -32765,
            the_large_int: -2147483645,
            the_big_int: -2147483645,
            general_int: -2147483645,
            the_date: chrono::NaiveDate::parse_from_str("2000-04-07", "%Y-%m-%d").unwrap(),
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-04-07T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("7fa8058e-840e-362e-a8e3-9d1b75f39fe8").unwrap(),
        },
        SampleWithIdRowData {
            the_name: "e".into(),
            the_small_int: -32764,
            the_large_int: -2147483644,
            the_big_int: -2147483644,
            general_int: -2147483644,
            the_date: chrono::NaiveDate::parse_from_str("2000-05-09", "%Y-%m-%d").unwrap(),
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-05-09T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("0edc1aeb-3200-3d26-b2be-77c5039aecf3").unwrap(),
        },
        SampleWithIdRowData {
            the_name: "f".into(),
            the_small_int: -32763,
            the_large_int: -2147483643,
            the_big_int: -2147483643,
            general_int: -2147483643,
            the_date: chrono::NaiveDate::parse_from_str("2000-06-11", "%Y-%m-%d").unwrap(),
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-06-11T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("9d6a2a8e-4852-33e4-ade0-7aaadb41066d").unwrap(),
        },
        SampleWithIdRowData {
            the_name: "g".into(),
            the_small_int: -32762,
            the_large_int: -2147483642,
            the_big_int: -2147483642,
            general_int: -2147483642,
            the_date: chrono::NaiveDate::parse_from_str("2000-07-13", "%Y-%m-%d").unwrap(),
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-07-13T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("f89673f7-afd4-3121-b58d-c3b683a88e4d").unwrap(),
        },
        SampleWithIdRowData {
            the_name: "h".into(),
            the_small_int: -32761,
            the_large_int: -2147483641,
            the_big_int: -2147483641,
            general_int: -2147483641,
            the_date: chrono::NaiveDate::parse_from_str("2000-08-15", "%Y-%m-%d").unwrap(),
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-08-15T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("9f56050f-c322-3efa-9e1f-96f4d3217930").unwrap(),
        },
        SampleWithIdRowData {
            the_name: "i".into(),
            the_small_int: -32760,
            the_large_int: -2147483640,
            the_big_int: -2147483640,
            general_int: -2147483640,
            the_date: chrono::NaiveDate::parse_from_str("2000-09-17", "%Y-%m-%d").unwrap(),
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-09-17T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("caa9705c-21b1-387a-afd8-de69e0698cb7").unwrap(),
        },
        SampleWithIdRowData {
            the_name: "j".into(),
            the_small_int: -32759,
            the_large_int: -2147483639,
            the_big_int: -2147483639,
            general_int: -2147483639,
            the_date: chrono::NaiveDate::parse_from_str("2000-10-19", "%Y-%m-%d").unwrap(),
            the_date_time: chrono::NaiveDateTime::parse_from_str(
                "2000-10-19T01:01",
                "%Y-%m-%dT%H:%M",
            )
            .unwrap(),
            the_uuid: uuid::Uuid::parse_str("0455e422-6d84-35e3-9db8-1eb8c06af841").unwrap(),
        },
    ]
}

/// Test by delete, bulk insert, select, bulk upsert, then delete for sample_with_id
#[tracing_test::traced_test]
#[serial_test::serial]
#[tokio::test]
pub async fn test_crud() {
    let pool = get_pool().await;
    let conn = pool.get().await.unwrap();
    // First delete all, assuming it worked
    let deleted = TableSampleWithId::delete_all(&conn).await.unwrap();
    tracing::info!("Initialize phase deleted {deleted}");

    /*
      Validate that delete work by selecting back an empty set
    */
    {
        assert_eq!(0, TableSampleWithId::select_all(&conn, 4).await.len());
    }
    let mut samples = get_sample_rows();

    /*
      Test the basic insert functionality
    */
    {
        let inserted = TableSampleWithId::basic_insert(&conn, samples.clone())
            .await
            .unwrap();

        tracing::debug!("Inserted with `basic_insert` -> {inserted:?}");

        /*
          Select back out the inserted data, convert to BTreeSet and compare to samples
        */
        {
            select_and_compare_assert(
                &conn,
                &inserted.into_iter().map(|r| r.data).collect(),
                "Basic Ins Cmp",
            )
            .await;
        }
        let deleted = TableSampleWithId::delete_all(&conn).await.unwrap();
        tracing::info!("Basic insert phase deleted {deleted}");
        assert_eq!(samples.len(), deleted as usize);
    }

    /*
      Test the bulk insert functionality
    */
    {
        let inserted = TableSampleWithId::bulk_insert(&conn, samples.clone(), 4)
            .await
            .unwrap();
        tracing::debug!("Inserted with `bulk_insert` -> {inserted:?}");
        /*
          Select back out the inserted data, convert to BTreeSet and compare to samples
        */
        select_and_compare_assert(
            &conn,
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
        let upserted = TableSampleWithId::bulk_upsert(&conn, samples.clone(), 4)
            .await
            .unwrap();
        tracing::debug!("Inserted with `bulk_upsert` -> {upserted:?}");
        select_and_compare_assert(&conn, &samples.iter().cloned().collect(), "Blk Upsert Cmp")
            .await;
    }

    /*
      Deleted all entries
    */
    {
        let deleted = TableSampleWithId::delete_all(&conn).await.unwrap();
        tracing::info!("Deleted all {deleted} TableSampleWithId entries");
        assert_eq!(deleted as usize, samples.len());
        let selected = TableSampleWithId::select_all(&conn, 4).await;
        assert_eq!(0, selected.len());
    }
}

// α <mod-def test_sample_with_id>
// ω <mod-def test_sample_with_id>
