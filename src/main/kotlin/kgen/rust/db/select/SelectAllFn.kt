package kgen.rust.db.select

import kgen.rust.*
import kgen.rust.db.TableGateway
import kgen.rust.db.clientFnParam

data class SelectAllFn(
    val tableGateway: TableGateway,
    val queryColumns: List<QueryColumn> = tableGateway.table.columns.map { it.asQueryColumn },
) {
    val selectAllFnImpl = SelectAllWhereFn(tableGateway, queryColumns)
    val returnType = this.selectAllFnImpl.returnType

    val selectAllFn get() = Fn(
        "select_all",
        "Select rows of `${this.selectAllFnImpl.id.snake}`",
        clientFnParam,
        FnParam("capacity", USize, "Capacity to the results"),
        isAsync = true,
        hasUnitTest = false,
        body = FnBody( """Self::select_all_where(&client, "1=1", &[], capacity).await"""),
        returnType = "Vec<$returnType>".asType,
        returnDoc = "Selected rows",
        inlineDecl = InlineDecl.Inline,
        testFnAttrs = attrSerializeTest.asAttrList
    )

}
