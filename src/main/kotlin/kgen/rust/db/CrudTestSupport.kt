package kgen.rust.db

import kgen.blockComment
import kgen.db.DbTableClassifier
import kgen.rust.*

data class CrudTestSupport(
    val tableGateway: TableGateway,
    val tableStruct: Struct,
) {

    /** The table modeled */
    val table get() = tableGateway.table

    /** Indicates table has _auto id_ */
    val hasAutoId get() = tableGateway.hasAutoId

    /** The classifier for the table type */
    val classifier get() = tableGateway.classifier

    val isKeyless get() = classifier == DbTableClassifier.Keyless

    /** The struct name for the type that provides the CRUD methods */
    val tableStructName = tableStruct.structName

    /** Uses required by the support test module */
    val uses = listOf(
        "super::support::get_pool",
        "super::support::MutateValue",
        "std::collections::BTreeSet",
        "std::ops::Deref",
        "tokio_postgres::Client"
    ).asUses

    /** Number of **literal** samples to generate for testing purposes */
    val sampleCount = 10

    /** Argument to bulk_(insert|upsert) - for _auto id_ give the samples, otherwise
     * lend them
     */
    val bulkSamplesArg = if (hasAutoId) {
        "samples.clone()"
    } else {
        "&samples"
    }

    val testFns = listOfNotNull(
        Fn(
            "mutate_row_data",
            """Mutate fields not appearing in pkey or unique keys for test purposes with pkey.
                |Only fields not appearing in unique keys allows stability in the uniqueness of rows.
            """.trimMargin(),
            FnParam("row_data", "& mut ${tableGateway.rowDataStructName}".asType, "Data to mutate"),
            body = FnBody(
                tableGateway
                    .dataQueryColumns
                    .queryColumns
                    .filter { queryColumn ->
                        queryColumn.dbColumn !in tableGateway.table.primaryKeyColumns &&
                                tableGateway.table.uniqueIndices.all { uniqueIndice ->
                                    queryColumn.dbColumn !in uniqueIndice.value
                                }
                    }
                    .joinToString("\n") {
                        "row_data.${it.id}.mutate_value();"
                    })
        ),

        Fn(
            "select_and_compare_assert",
            "Select all from the database and assert they compare to [values]",
            FnParam("pool_conn", "&T".asType, "The pool connection"),
            FnParam(
                "values", "&Vec<${tableGateway.rowDataStructName}>".asType,
                "Values to compare to selected"
            ),
            FnParam("label", "&str".asType, "Label for assert"),
            isAsync = true,
            genericParamSet = GenericParamSet(
                TypeParam(
                    "t",
                    bounds = Bounds("Deref<Target = Client>")
                )
            ),
            body = FnBody(
                listOf(
                    when (tableGateway.classifier) {
                        DbTableClassifier.AutoIdWithPkey, DbTableClassifier.AutoId -> {
                            """
   let selected_entries = ${tableStructName}::select_all(&pool_conn, 4).await;
   let selected = entries_to_row_data(&selected_entries);
            """.trimIndent()
                        }

                        else -> {
                            """
   let selected = ${tableStructName}::select_all(&pool_conn, 4).await;
            """.trimIndent()
                        }
                    },
                    """
assert_eq!(selected.len(), values.len());
get_sample_rows_sorted(&selected).iter().zip(get_sample_rows_sorted(values).iter()).for_each(|(a, b)| {
    let matched = a == b;
    tracing::debug!("{label}: {}", if matched { format!("Match({a:?})") } else { format!("Mismatch\n{a:?}\n---\n{b:?}") }); 
    assert_eq!(true, matched);
});
                """.trimIndent()
                ).joinToString("\n"),
            )
        ),
        when (tableGateway.classifier) {
            DbTableClassifier.AutoId, DbTableClassifier.AutoIdWithPkey ->
                Fn(
                    "entries_to_row_data",
                    "Convert entries that include both auto-id and data into the data portion",
                    FnParam("entries", "&[${tableGateway.rowEntryStructName!!}]".asType, "The entries"),
                    returnType = "Vec<${tableGateway.rowDataStructName}>".asType,
                    returnDoc = "The data portion of the entries",
                    body = FnBody("entries.iter().cloned().map(|e| e.data).collect()"),
                    inlineDecl = InlineDecl.Inline
                )

            else -> null
        },
        Fn(
            "get_sample_rows_sorted",
            "Get the sample rows as a set",
            FnParam("rows", "&[${tableGateway.rowDataStructName}]".asType, "The rows to stringify and sort"),
            body = FnBody("rows.iter().cloned().map(|r| format!(\"{r:?}\")).collect()"),
            returnType = "BTreeSet<String>".asType,
            returnDoc = "The samples as set",
            inlineDecl = InlineDecl.Inline
        ),
        Fn(
            "get_sample_rows",
            "Get a set of sample rows for testing",
            body = FnBody(
                listOf(
                    "vec![",
                    run {
                        val generators = tableGateway.dataQueryColumns.queryColumns
                            .associateWith { it.type.getSampleIterator() }
                        (0 until sampleCount)
                            .joinToString(",\n") {
                                listOf(
                                    "${tableGateway.rowDataStructName} {",
                                    generators.entries.joinToString(",\n") { (queryColumn, generator) ->
                                        "${queryColumn.id.snake}: ${
                                            queryColumn.asRustLiteral(
                                                generator.next().toString()
                                            )
                                        }"
                                    },
                                    "}"
                                ).joinToString("\n")
                            }
                    },
                    "]"
                ).joinToString("\n")
            ),
            returnType = "Vec<${tableGateway.rowDataStruct.structName}>".asType,
            returnDoc = "Set of sample rows to test CRUD methods"
        ),
        Fn(
            "test_crud",
            "Test by delete, bulk insert, select, bulk upsert, then delete for ${table.nameId}",
            isTokioTest = true,
            attrs = listOf(attrTracingTest, attrSerializeTest).asAttrList,
            body = listOf(
                """
let pool = get_pool().await;
let conn = pool.get().await.unwrap();
// First delete all, assuming it worked
let deleted = ${tableStructName}::delete_all(&conn).await.unwrap();
tracing::info!("Initialize phase deleted {deleted}");

${"Validate that delete work by selecting back an empty set".blockComment}
{
    assert_eq!(0, ${tableStructName}::select_all(&conn, 4).await.len());
}
let ${
                    // Keyless does not support upsert since no key - so no mutation
                    if (isKeyless) {
                        "samples"
                    } else {
                        "mut samples"
                    }
                } = get_sample_rows();

${"Test the basic insert functionality".blockComment}
{
    let inserted = ${tableStructName}::basic_insert(&conn, ${
                    if (hasAutoId) {
                        "samples.clone()"
                    } else {
                        "&samples"
                    }
                }).await.unwrap();
                
    tracing::debug!("Inserted with `basic_insert` -> {inserted:?}");
    
    ${"Select back out the inserted data and compare to samples".blockComment}
    {
        select_and_compare_assert(&conn, ${
                    tableGateway.autoIdDetails?.insertedDataTransform ?: "&get_sample_rows().iter().cloned().collect()"
                }, "Basic Ins Cmp").await;
    }
    let deleted = ${tableStructName}::delete_all(&conn).await.unwrap();
    tracing::info!("Basic insert phase deleted {deleted}");
    assert_eq!(samples.len(), deleted as usize);
}

${"Test the bulk insert functionality".blockComment}
{
    let inserted = ${tableStructName}::bulk_insert(&conn, $bulkSamplesArg, 4).await.unwrap();
    tracing::debug!("Inserted with `bulk_insert` -> {inserted:?}");
    ${"Select back out the inserted data and compare to samples".blockComment}
    select_and_compare_assert(&conn, ${tableGateway.autoIdDetails?.insertedDataTransform ?: "&get_sample_rows().iter().cloned().collect()"}, "Blk Ins Cmp").await;
}

${
                    if (!isKeyless) {
                        """
${"Mutate the data, and bulk upsert.".blockComment}
{
    samples.iter_mut().for_each(|data| mutate_row_data(data));
    tracing::debug!("Mutated Samples: {samples:?}");
    let upserted = ${tableStructName}::bulk_upsert(&conn, $bulkSamplesArg, 4).await.unwrap();
    tracing::debug!("Inserted with `bulk_upsert` -> {upserted:?}");
    select_and_compare_assert(&conn, &samples.iter().cloned().collect(), "Blk Upsert Cmp").await;
}    
      """.trimIndent()
                    } else {
                        ""
                    }
                }
                
${"Deleted all entries".blockComment}
{
    let deleted = ${tableStructName}::delete_all(&conn).await.unwrap();
    tracing::info!("Deleted all {deleted} ${tableStructName} entries");
    assert_eq!(deleted as usize, samples.len());
    let selected = ${tableStructName}::select_all(&conn, 4).await;
    assert_eq!(0, selected.len());
}
            """.trimIndent()
            )
                .joinToString("\n")
                .asFnBody,
            uses = listOf("kgen_db::${table.id}::*").asUses,
        )
    )
}