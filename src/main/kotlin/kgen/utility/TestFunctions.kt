package kgen.utility

import kgen.Id
import kgen.rust.*

fun panicTest(nameId: String) = Fn(
    nameId,
    doc = null,
    attrs = AttrList(attrTestFn, Attr.Word("should_panic")),
    emptyBlockContents = """todo!("Add test $nameId")""",
    visibility = Visibility.None
)

fun panicTest(id: Id) = panicTest(id.snakeCaseName)

fun unitTest(nameId: String, attrTestFn: Attr) = Fn(
    nameId,
    doc = null,
    attrs = AttrList(attrTestFn, attrTracingTest),
    emptyBlockContents = """todo!("Add test $nameId")""",
    visibility = Visibility.None,
    isAsync = attrTestFn == attrTokioTestFn
)

fun unitTest(id: Id, blockName: String, attrTestFn: Attr) = Fn(
    id.snakeCaseName,
    blockName = blockName,
    emptyBlockContents = """todo!("Test ${id.snakeCaseName}")""",
    doc = null,
    attrs = AttrList(attrTestFn, attrTracingTest),
    visibility = Visibility.None,
    isAsync = attrTestFn == attrTokioTestFn
)