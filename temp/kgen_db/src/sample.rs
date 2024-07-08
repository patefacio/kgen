//! Table gateway pattern implemented for Sample

////////////////////////////////////////////////////////////////////////////////////
// --- type aliases ---
////////////////////////////////////////////////////////////////////////////////////
/// Rows are composed of the primary key and the value fields
pub type SampleRow = (SamplePkey, SampleValues);

////////////////////////////////////////////////////////////////////////////////////
// --- structs ---
////////////////////////////////////////////////////////////////////////////////////
/// Primary key fields for `Sample`
pub struct SamplePkey {
    /// Field for column `id`
    pub id: String,
}

/// Primary key fields for `Sample`
pub struct SampleValues {
    /// Field for column `the_name`
    pub the_name: String,
    /// Field for column `the_small_int`
    pub the_small_int: String,
    /// Field for column `the_large_int`
    pub the_large_int: String,
    /// Field for column `general_int`
    pub general_int: String,
    /// Field for column `the_date`
    pub the_date: String,
    /// Field for column `the_date_time`
    pub the_date_time: String,
    /// Field for column `the_uuid`
    pub the_uuid: String,
    /// Field for column `the_ulong`
    pub the_ulong: String,
}

/// Table Gateway Support for table `sample`.
/// Rows
pub struct TableSample {}

////////////////////////////////////////////////////////////////////////////////////
// --- type impls ---
////////////////////////////////////////////////////////////////////////////////////
impl TableSample {
    /// Insert rows of `sample`
    ///
    ///   * **client** - The tokio postgresl client
    ///   * **rows** - Rows to insert
    pub async fn insert(client: tokio_postgres::Client, rows: &[SampleRow]) {
        // α <fn TableSample::insert>
        todo!("Implement `insert`")
        // ω <fn TableSample::insert>
    }

    /// Select rows of `sample`
    pub async fn select() {
        // α <fn TableSample::select>
        todo!("Implement `select`")
        // ω <fn TableSample::select>
    }

    /// Update rows of `sample`
    pub async fn update() {
        // α <fn TableSample::update>
        todo!("Implement `update`")
        // ω <fn TableSample::update>
    }

    /// Delete rows of `sample`
    pub async fn delete() {
        // α <fn TableSample::delete>
        todo!("Implement `delete`")
        // ω <fn TableSample::delete>
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
        #[tokio::test]
        #[tracing_test::traced_test]
        async fn insert() {
            // α <fn test TableSample::insert>
            use tokio_postgres::types::{FromSql, ToSql, Date};
            use tokio_postgres::NoTls;

            let (client, connection) =
                tokio_postgres::connect("host=localhost user=kgen password=kgen dbname=kgen", NoTls).await.unwrap();

            tokio::spawn(async move {
                if let Err(e) = connection.await {
                    eprintln!("connection error: {}", e);
                }
            });

            tracing::info!("Created {client:?}");

            let mut params = Vec::<&(dyn ToSql + Sync)>::with_capacity(34);
            let statement = r#"insert into sample
            (the_name, the_small_int, the_large_int, general_int, the_date, the_date_time, the_uuid, the_ulong)
            values
            ($1, $2, $3, $4, $5, $6, $7, $8),
            ($9, $10, $11, $12, $13, $14, $15, $16)
            returning id
            "#.to_string();
            params.push(&"a");
            params.push(&1i16);
            params.push(&2i64);
            params.push(&3i32);
            params.push(&chrono::NaiveDate::MAX);
            params.push(&chrono::NaiveDateTime::MAX);
            params.push(&uuid::uuid!("123e4567-e89b-12d3-a456-426655440000"));
            params.push(&32i64);

            params.push(&"b");
            params.push(&1i16);
            params.push(&2i64);
            params.push(&3i32);
            params.push(&chrono::NaiveDate::MAX);
            params.push(&chrono::NaiveDateTime::MAX);
            params.push(&uuid::uuid!("123e4567-e89b-12d3-a456-426655440000"));
            params.push(&32i64);


            let results = match client.query(&statement, &params[..]).await {
                Ok(stmt) => stmt,
                Err(e) => {
                    panic!("Error preparing statement: {e}");
                }
            };


            results.iter().for_each(|row| tracing::info!("Row id -> {:?}", row.get::<usize, i32>(0)))

            // ω <fn test TableSample::insert>
        }

        #[tokio::test]
        #[tracing_test::traced_test]
        async fn select() {
            // α <fn test TableSample::select>
            todo!("Test select")
            // ω <fn test TableSample::select>
        }

        #[tokio::test]
        #[tracing_test::traced_test]
        async fn update() {
            // α <fn test TableSample::update>
            todo!("Test update")
            // ω <fn test TableSample::update>
        }

        #[tokio::test]
        #[tracing_test::traced_test]
        async fn delete() {
            // α <fn test TableSample::delete>
            todo!("Test delete")
            // ω <fn test TableSample::delete>
        }

        // α <mod-def test_table_sample>
        // ω <mod-def test_table_sample>
    }

    // α <mod-def unit_tests>
    // ω <mod-def unit_tests>
}

// α <mod-def sample>
// ω <mod-def sample>
