package kgen.rust.db

import kgen.Id

/** Modeled rust sql statement supporting literal `&str` or formatted string
 * when required.
 *
 * @property varName Name of the rust statement variable
 * @property sqlStatement The statement modeled
 * @property backdoorId If table has backdoor support the id to replace original table name with
 * @property formatRequired If true, the SQL statement has embedded strings and requires `format!(...)`
 */
data class RustSqlStatement(
    val varName: String,
    val sqlStatement: String,
    val backdoorId: Id?,
    val formatRequired: Boolean = false,
) {
    val letStatement get() = if(backdoorId != null) {
        val patched = sqlStatement.replace(backdoorId.snake, "{}")
        "let $varName = format!($patched, *${backdoorId.shout}_TABLE_NAME);"
    } else if(formatRequired) {
        "let $varName = format!($sqlStatement);"
    } else {
        "let $varName = $sqlStatement;"
    }

    val asStr get() = if(backdoorId != null || formatRequired) {
        "&$varName"
    } else { varName }
}