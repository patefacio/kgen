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

/** Ensure test has either `#[test]` or `#[tokio::test]` and has tracing test set */
private fun ensureIsTest(testFnAttrs: AttrList) =
    when {
        testFnAttrs.attrs.contains(attrTestFn) -> testFnAttrs.attrs
        testFnAttrs.attrs.contains(attrTokioTestFn) -> testFnAttrs.attrs
        else -> testFnAttrs.attrs.plus(attrTestFn)
    }.plus(attrTracingTest).toSortedSet(compareBy { it.asOuterAttr }).toList().asAttrList

fun unitTest(nameId: String, testFnAttrs: AttrList) = Fn(
    nameId,
    doc = null,
    attrs = ensureIsTest(testFnAttrs),
    emptyBlockContents = """todo!("Add test $nameId")""",
    visibility = Visibility.None,
    isAsync = attrTokioTestFn in testFnAttrs.attrs
)

fun unitTest(id: Id, blockName: String, testFnAttrs: AttrList = AttrList()) = Fn(
    id.snakeCaseName,
    blockName = blockName,
    emptyBlockContents = """todo!("Test ${id.snakeCaseName}")""",
    doc = null,
    attrs = ensureIsTest(testFnAttrs),
    visibility = Visibility.None,
    isAsync = attrTokioTestFn in testFnAttrs.attrs
)