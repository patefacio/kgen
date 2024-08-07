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
// --- structs ---
////////////////////////////////////////////////////////////////////////////////////
/// Primary key fields for `Sample`
pub struct SamplePkey {
    /// Field for column `id`
    pub id: i32,
}

/// Value fields for `Sample`
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
        let col_num = 9;
        let row_num = rows.len();
        let mut params = Vec::<&(dyn ToSql + Sync)>::with_capacity(row_num * col_num);

        let mut values_placeholder = "".to_string();
        for i in 0..row_num {
            values_placeholder += "(";
            for j in 1..col_num {
                values_placeholder += &format!("${}, ", j + col_num * i);
            }
            values_placeholder += &format!("${}),\n", col_num * (i + 1));
        }
        values_placeholder = values_placeholder[0..values_placeholder.len() - 2].to_string();

        //println!("{values_placeholder}");

        let statement = format!(
            r#" 
            insert into sample (
              id, the_name, the_small_int, the_large_int, general_int, the_date,
              the_date_time, the_uuid, the_ulong
            )
            values
            {values_placeholder}
            returning id
            "#
        )
        .to_string();

        for row in rows {
            params.push(&row.0.id);
            params.push(&row.1.the_name);
            params.push(&row.1.the_small_int);
            params.push(&row.1.the_large_int);
            params.push(&row.1.general_int);
            params.push(&row.1.the_date);
            params.push(&row.1.the_date_time);
            params.push(&row.1.the_uuid);
            params.push(&row.1.the_ulong)
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

    /// Select rows of `sample`
    ///
    ///   * **client** - The tokio postgresl client
    pub async fn select(client: tokio_postgres::Client) {
        //HERE
    }

    /// Update rows of `sample`
    ///
    ///   * **client** - The tokio postgresl client
    ///   * **s_clause** - clause for SET statement
    ///   * **w_clause** - clause for WHERE statement
    pub async fn update(client: tokio_postgres::Client, s_clause: String, w_clause: String) {
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
    ///   * **client** - The tokio postgresl client
    ///   * **clause** - full clause, skips input vectors
    ///   * **cols** - columns list for clause
    ///   * **ops** - operator list for clause
    ///   * **conds** - conditions list for clause
    pub async fn delete(
        client: tokio_postgres::Client,
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
            use crate::sample::*;
            use crate::SampleRow;

            let (client, connection) =
                tokio_postgres::connect("host=localhost user=kgen password=kgen dbname=kgen", NoTls).await.unwrap();

            tokio::spawn(async move {
                if let Err(e) = connection.await {
                    eprintln!("connection error: {}", e);
                }
            });

            tracing::info!("Created {client:?}");

            let row_1 : SampleRow = (SamplePkey{id: 1}, SampleValues{ the_name: "TEST ROW 1".to_string(), the_small_int: 1i16, the_large_int: 2i64, general_int: 3i32, the_date: chrono::NaiveDate::MAX, the_date_time: chrono::NaiveDateTime::MAX, the_uuid: uuid::uuid!("123e4567-e89b-12d3-a456-426655440000"), the_ulong: 32i64 });
            let row_2 : SampleRow = (SamplePkey{id: 2}, SampleValues{ the_name: "TEST ROW 2".to_string(), the_small_int: 51i16, the_large_int: -213i64, general_int: 73i32, the_date: chrono::NaiveDate::MAX, the_date_time: chrono::NaiveDateTime::MAX, the_uuid: uuid::uuid!("765e4321-e89b-12d3-a456-426655440000"), the_ulong: 34i64 });
            let row_3 : SampleRow = (SamplePkey{id: 3}, SampleValues{ the_name: "TEST ROW 3".to_string(), the_small_int: 51i16, the_large_int: -213i64, general_int: 73i32, the_date: chrono::NaiveDate::MAX, the_date_time: chrono::NaiveDateTime::MAX, the_uuid: uuid::uuid!("765e4321-e89b-12d3-a456-426655440000"), the_ulong: 34i64 });


            let rows_list: [SampleRow; 3] = [row_1, row_2, row_3];
            TableSample::insert(client, &rows_list).await;

            // ω <fn test TableSample::insert>
        }

        #[tokio::test]
        #[tracing_test::traced_test]
        async fn select() {
            // α <fn test TableSample::select>
            // TESTING GITHUB ACTIONS
            use tokio_postgres::types::{FromSql, ToSql, Date};
            use tokio_postgres::NoTls;
            use crate::sample::*;
            use crate::SampleRow;

            let (client, connection) =
                tokio_postgres::connect("host=localhost user=kgen password=kgen dbname=kgen", NoTls).await.unwrap();

            tokio::spawn(async move {
                if let Err(e) = connection.await {
                    eprintln!("connection error: {}", e);
                }
            });

            tracing::info!("Created {client:?}");

            let mut input_cols = vec!["id","the_name","general_int"];
            let mut input_operator = vec!["=","=","<"];
            let mut input_condition = vec!["2", "\'TEST ROW 3\'", "5"];

            let clause = "id >= 2 OR the_name = 'TEST ROW 3' OR general_int = 3".to_string();



            //TableSample::select(client, clause, input_cols, input_operator, input_condition).await;
            // ω <fn test TableSample::select>
        }

        #[tokio::test]
        #[tracing_test::traced_test]
        async fn update() {
            // α <fn test TableSample::update>
            use tokio_postgres::types::{FromSql, ToSql, Date};
            use tokio_postgres::NoTls;
            use crate::sample::*;
            use crate::SampleRow;

            let (client, connection) =
                tokio_postgres::connect("host=localhost user=kgen password=kgen dbname=kgen", NoTls).await.unwrap();

            tokio::spawn(async move {
                if let Err(e) = connection.await {
                    eprintln!("connection error: {}", e);
                }
            });

            tracing::info!("Created {client:?}");

            let where_clause = "the_name LIKE 'TEST ROW%'".to_string();
            let set_clause = "the_name = 'UPDATED ROW 3'".to_string();

            TableSample::update(client, set_clause, where_clause).await;
            // ω <fn test TableSample::update>
        }

        #[tokio::test]
        #[tracing_test::traced_test]
        async fn delete() {
            // α <fn test TableSample::delete>
            use tokio_postgres::types::{FromSql, ToSql, Date};
            use tokio_postgres::NoTls;
            use crate::sample::*;
            use crate::SampleRow;

            let (client, connection) =
                tokio_postgres::connect("host=localhost user=kgen password=kgen dbname=kgen", NoTls).await.unwrap();

            tokio::spawn(async move {
                if let Err(e) = connection.await {
                    eprintln!("connection error: {}", e);
                }
            });

            tracing::info!("Created {client:?}");

            let mut input_cols = vec!["id","the_name","general_int"];
            let mut input_operator = vec!["=","=","<"];
            let mut input_condition = vec!["2", "\'TEST ROW 3\'", "5"];

            let clause = "id = 2 OR the_name LIKE '%ROW 3' OR general_int = 3".to_string();


            TableSample::delete(client, clause, input_cols, input_operator, input_condition).await;


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
