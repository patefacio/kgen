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

fun unitTest(nameId: String, testFnAttrs: AttrList) = Fn(
    nameId,
    doc = null,
    attrs = testFnAttrs,
    emptyBlockContents = """todo!("Add test $nameId")""",
    visibility = Visibility.None,
    isAsync = attrTokioTestFn in testFnAttrs.attrs
)

fun unitTest(id: Id, blockName: String, testFnAttrs: AttrList) = Fn(
    id.snakeCaseName,
    blockName = blockName,
    emptyBlockContents = """todo!("Test ${id.snakeCaseName}")""",
    doc = null,
    attrs = testFnAttrs,
    visibility = Visibility.None,
    isAsync = attrTokioTestFn in testFnAttrs.attrs
)