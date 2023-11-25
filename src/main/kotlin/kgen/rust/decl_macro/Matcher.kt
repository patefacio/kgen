package kgen.rust.decl_macro

import kgen.rust.AsRust

sealed class FragSpec : AsRust {
    data object Block : FragSpec() {
        override val asRust: String
            get() = "block"
    }

    data object Expr : FragSpec() {
        override val asRust: String
            get() = "expr"
    }

    data object Ident : FragSpec() {
        override val asRust: String
            get() = "ident"
    }

    data object Item : FragSpec() {
        override val asRust: String
            get() = "item"
    }

    data object Meta : FragSpec() {
        override val asRust: String
            get() = "meta"
    }

    data object Pat : FragSpec() {
        override val asRust: String
            get() = "pat"
    }

    data object PatParam : FragSpec() {
        override val asRust: String
            get() = "pat_param"
    }

    data object Path : FragSpec() {
        override val asRust: String
            get() = "path"
    }

    data object Stmt : FragSpec() {
        override val asRust: String
            get() = "stmt"
    }

    data object TokenTree : FragSpec() {
        override val asRust: String
            get() = "tt"
    }

    data object Ty : FragSpec() {
        override val asRust: String
            get() = "ty"
    }

    data object Vis : FragSpec() {
        override val asRust: String
            get() = "vis"
    }
}

fun expr(nameId: String, repOp: RepOp = RepOp.One) =
    Matcher(nameId, FragSpec.Expr, repOp)

fun tokenTree(nameId: String, repOp: RepOp = RepOp.One) =
    Matcher(nameId, FragSpec.TokenTree, repOp)

/** Repetition Operator
 *
 */
sealed

class RepOp {
    data object ZeroOrMore : RepOp()
    data object OneOrMore : RepOp()
    data object ZeroOrOne : RepOp()
    data object One : RepOp()
}

data class Matcher(
    val nameId: String,
    val fragSpec: FragSpec,
    val repOp: RepOp = RepOp.One
) : AsRust {

    val asFragId get() = "$nameId:${fragSpec.asRust}"
    override val asRust: String
        get() = when (repOp) {
            is RepOp.One -> "\$$asFragId"
            is RepOp.OneOrMore -> "\$(\$$asFragId)+"
            is RepOp.ZeroOrOne -> "\$(\$$asFragId)?"
            is RepOp.ZeroOrMore -> "\$(\$$asFragId)*"
        }
}
