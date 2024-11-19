package kgen.rust.db

import kgen.indent
import kgen.rust.Fn
import kgen.rust.FnBody
import kgen.rust.asAttrList
import kgen.rust.attrSerializeTest
import kgen.rustQuote

data class BasicInsert(
    val tableGateway: TableGateway,
) {
    val id get() = tableGateway.id
    val table = tableGateway.table
    val insertBody = "todo!()"
    val insertStatement get() =
        Fn(
            "insert",
            "Insert rows of `${id.snake}`",
            clientFnParam,
            tableGateway.rowsParam,
            isAsync = true,
            hasTokioTest = true,
            body = FnBody(insertBody),
            testFnAttrs = attrSerializeTest.asAttrList
        )

}
