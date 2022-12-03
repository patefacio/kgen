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

fun unitTest(nameId: String) = Fn(
    nameId,
    doc = null,
    attrs = AttrList(attrTestFn),
    emptyBlockContents = """todo!("Add test $nameId")""",
    visibility = Visibility.None
)

fun unitTest(id: Id, blockName: String) = Fn(
    id.snakeCaseName,
    blockName = blockName,
    emptyBlockContents = """todo!("Test ${id.snakeCaseName}")""",
    doc = null,
    attrs = AttrList(attrTestFn),
    visibility = Visibility.None
)