package kgen.rust

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AttrTest {
    @Test
    fun attrBasics() {
        assertEquals(
            "#[foo]",
            Attr.Word("foo").asOuterAttr
        )

        assertEquals(
            "#[macro_use(a, b)]",
            Attr.Words("macro_use", "a", "b").asOuterAttr
        )

        assertEquals(
            "#[foo=\"bar\"]",
            Attr.Value("foo", "bar").asOuterAttr
        )

        assertEquals(
            "#[foo(age=\"3\", name=\"bar\")]",
            Attr.Dict("foo", "name" to "bar", "age" to "3").asOuterAttr
        )

        // Leptos
        assertEquals("#[cfg(feature=\"ssr\")]", attrSsr.asOuterAttr)
        assertEquals("#[component]", attrComponent.asOuterAttr)

        assertEquals("#[cfg(test)]", attrCfgTest.asOuterAttr)
        assertEquals("#[inline]", attrInline.asOuterAttr)
        assertEquals("#[dynamic]", attrDynamic.asOuterAttr)
        assertEquals("#[derive(Debug)]", attrDeriveDebug.asOuterAttr)
        assertEquals("#[derive(Default)]", attrDeriveDefault.asOuterAttr)
        assertEquals("#[derive(Clone)]", attrDeriveClone.asOuterAttr)
        assertEquals("#[derive(Copy)]", attrDeriveCopy.asOuterAttr)

        assertEquals("#[derive(Serialize, Deserialize)]", attrSerdeSerialization.asOuterAttr)
        assertEquals("#[cfg(debug_assertions)]", attrDebugBuild.asOuterAttr)
        assertEquals("#[cfg(not(debug_assertions))]", attrNotDebugBuild.asOuterAttr)
        assertEquals("#[test]", attrTestFn.asOuterAttr)

        assertEquals("#[allow(unused)]", attrAllowUnused.asOuterAttr)
        assertEquals("#[feature(iter_intersperse)]", attrIterIntersperse.asOuterAttr)
        assertEquals("#[cfg_attr(debug_assertions, allow(unused_variables))]", attrDebugUnusedVariables.asOuterAttr)
        assertEquals("#[allow(unused)]", attrAllowUnused.asOuterAttr)

        assertEquals("#[feature(variant_count)]", attrVariantCount.asOuterAttr)
        assertEquals("#[feature(is_sorted)]", attrIsSorted.asOuterAttr)
        assertEquals("#[deny(missing_docs)]", attrDenyMissingDoc.asOuterAttr)
        assertEquals("#[template(escape=\"none\")]", attrNoEscapeTemplate.asOuterAttr)


    }
}