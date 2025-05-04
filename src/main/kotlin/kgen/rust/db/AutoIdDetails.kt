package kgen.rust.db

import kgen.Id
import kgen.rust.FnParam
import kgen.rust.asType

/** Place to put helper values when generating for tables with _auto id_. */
data class AutoIdDetails(
    val autoId: Id,
    val rowEntryStructName: String,
    val rowDataStructName: String
) {

    /** Output type for _bulk insert_ - vector with _auto ids_ returned if table has _auto id_, unit */
    val outputType = "Vec<$rowEntryStructName>"

    val collectResult = """
$autoId.into_iter()
       .zip(rows.into_iter())
       .map(|($autoId, data)| $rowEntryStructName { $autoId, data })
       .collect()
    """.trimIndent()

    /** _auto id_ let binding to get the id's returned from the insert */
    val autoIdVecLet = "\nlet mut $autoId = Vec::with_capacity(rows.len());\n"

    val pushAutoId
        get() = """
chunk_result.iter().for_each(|result| {
    $autoId.push(result.get(0));
});      
    """.trimIndent()

    val collectAutoId
        get() = """
insert_result
    .into_iter()
    .zip(rows)
    .map(|(row, data)| {
        let $autoId = row.get(0);
        ${rowEntryStructName} { $autoId, data }
    })
    .collect()     
    """.trimIndent()

    /** Input param type for _bulk insert_ - consumed vector if table has _auto id_, else slice */
    val inputFnParam = FnParam(
        "rows",
        "Vec<$rowDataStructName>".asType,
        "Row data, consumed but returned with ids"
    )

    val insertReturnDoc = "Entries with corresponding _auto_id_"

    /** The transform of an insertion returning entries to the data vector */
    val insertedDataTransform = "&inserted.into_iter().map(|r| r.data).collect()"

}