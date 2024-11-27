package kgen.rust.db

import kgen.rust.FnParam
import kgen.rust.asType

val clientFnParam = FnParam("client", "&Client".asType, "The tokio postgresql client")
