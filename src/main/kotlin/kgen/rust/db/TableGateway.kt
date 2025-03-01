package kgen.rust.db

import kgen.Id
import kgen.asId
import kgen.db.DbTable
import kgen.db.DbTableClassifier
import kgen.doubleQuote
import kgen.markdownQuoteComment
import kgen.rust.*
import kgen.rust.db.select.*

/** Responsible for providing CRUD rust code generation roughly following the _Table Gateway Pattern_
 * @property table The table to generate crud for.
 * @property backdoorTable In a team dev environment can be convenient to hit alternate tables name
 * so this allows Joe to do `TABLE_MY_TABLE=my_table_joe cargo run ...`.
 */
data class TableGateway(
    val table: DbTable,
    val backdoorTable: Boolean = false,
) {
    /** [kgen.Id] used to base table related names */
    val id = table.nameId.asId

    /** Classifier determining the types needed for supporting CRUD ops for the table */
    val classifier = table.classifier

    /** Indicates presence of an _auto id_ */
    val hasAutoId get() = classifier.hasAutoId

    /** Count of all columns in the table */
    val columnCount = table.columns.size

    /** Various rust type ids and types involved in supporting the CRUD functions */
    val gatewayTypes = GatewayTypes.fromClassifier(id, classifier)

    /** The [Id] for the `RowData` type */
    val rowDataId get() = gatewayTypes.rowDataId

    /** The set of all table [QueryColumn]s */
    val allQueryColumns = table.columns.map { it.asQueryColumn }

    /** The set of all table [QueryColumn]s, excluding any _auto inc_ column.
     * If the table has an _auto inc_ column it is kept separate from the [dataQueryColumns]
     * for the cases when the _auto inc_ is not relevant. For example, when inserting rows
     * no _auto inc_ would be supplied, only the _data query columns_.
     */
    val dataQueryColumns = QueryColumnSet(
        gatewayTypes.rowDataId.snake, "Primary data fields",
        allQueryColumns.filter { !it.isAutoInc }
    )


    /** If using back door the name of a static table name string that has checked env var
     * for override.
     */
    val tableNameVarId: Id?

    /** If using back door the static name */
    val tableNameStatic: Static?

    fun formatStatement(statement: String) =
        if (backdoorTable) {
            val patched = statement.replace(id.snake, "{}")
            "format!($patched, *${tableNameVarId!!.shout})"
        } else {
            "format!($statement)"
        }

    init {
        if (backdoorTable) {
            tableNameVarId = "${id}_table_name".asId
            tableNameStatic = Static(
                tableNameVarId.snake, "Name of table accessed",
                "LazyLock<String>".asType,
                value = StaticValue(
                    """std::sync::LazyLock::new(||
                    |std::env::var("${tableNameVarId.shout}")
                    |.unwrap_or(${doubleQuote(id.snake)}.into()))
                    |""".trimMargin()
                )
            )
        } else {
            tableNameVarId = null
            tableNameStatic = null
        }
    }


    val rowDataStruct = dataQueryColumns.asRustStruct
    val rowDataStructName = rowDataStruct.structName

    val autoIncQueryColumn = allQueryColumns.firstOrNull { it.isAutoInc }

    val nonAutoIncColumnSetLiteralValue = "(\n\t${table.nonAutoIncGroupedColumnNames}\n)"
    val bulkInsertChunkSizeFnParam = FnParam("chunk_size", USize, "How to chunk the inserts")
    val unnestedColumnExpressionValue = "(\n\t${table.unnestedColumnExpressions}\n)"

    val rowEntryStruct = autoIncQueryColumn?.let {
        Struct(
            "${id}_entry",
            """All fields plus auto id for table `${table.id}`.""",
            it.dbColumn!!.asRustField,
            Field("data", "The data fields", rowDataId.capCamel.asType),
            attrs = commonDerives
        )
    }

    val rowEntryStructName = rowEntryStruct?.asRustName

    val autoIdDetails = if (hasAutoId) {
        AutoIdDetails(
            autoIncQueryColumn!!.id,
            rowEntryStructName!!,
            rowDataStructName
        )
    } else null

    val selectAllWhere = SelectAllWhereFn(this)
    val selectAll = SelectAllFn(this)
    val basicInsert = BasicInsert(this, autoIdDetails)
    val bulkInsert = BulkInsert(this, autoIdDetails)
    val bulkUpsert = BulkUpsert(this, autoIdDetails)
    val keyColumnSet = if (table.hasPrimaryKey) {
        QueryColumnSet(
            "${id.snake}_pkey",
            "Primary key fields for `${id.capCamel}`",
            table.primaryKeyColumns.asQueryColumns
        )
    } else {
        null
    }

    val keyStruct = keyColumnSet?.asRustStruct

    val tableStruct = Struct(
        "table_${id.snake}",
        """Table Gateway Support for table `${id.snake}`.
            |${table.doc ?: ""}
        """.trimMargin(),
        consts = listOf(
            Const(
                "column_count",
                "The total number of key and value columns",
                USize,
                columnCount
            )
        ),
        typeImpl = TypeImpl(
            "Table${id.capCamel}".asType,
            functions = listOfNotNull(
                selectAllWhere.selectAllWhereFn,
                selectAll.selectAllFn,
                basicInsert.basicInsertFn,
                bulkInsert.bulkInsertFn,
                bulkUpsert.bulkUpsertFn,
            ) +
                    Fn(
                        "delete_all",
                        "Delete all rows of `$id`",
                        clientFnParam,
                        genericParamSet = genericClientParamSet,
                        isAsync = true,
                        hasTokioTest = true,
                        inlineDecl = InlineDecl.Inline,
                        returnType = "Result<u64, tokio_postgres::Error>".asType,
                        returnDoc = "Number of rows deleted",
                        body = FnBody("""client.execute(&${formatStatement(doubleQuote("DELETE FROM $id"))}, &[]).await"""),
                        hasUnitTest = false
                    ),
        ),
        attrs = commonDerives + derive("Default")
    )


    val asModule = Module(
        table.nameId,
        listOfNotNull(
            """Table gateway pattern implemented for ${id.capCamel}""",
            table.doc?.markdownQuoteComment
        ).joinToString("\n\n"),
        uses = listOf(
            "tokio_postgres::types::ToSql",
            "tokio_postgres::Client",
        ).asUses + Use("std::sync::LazyLock", attrAllowUnused),
        structs = listOfNotNull(
            rowDataStruct, rowEntryStruct,
            keyStruct, tableStruct
        ),
        statics = listOfNotNull(tableNameStatic)
    )

    val crudTestSupport = CrudTestSupport(this, tableStruct)

    val testModule = Module(
        "test_${table.nameId}",
        """Tests for ${table.nameId} table""",
        functions = crudTestSupport.testFns,
        uses = crudTestSupport.uses
    )

    companion object {

        val mutateValueTrait = Trait(
            "mutate_value",
            "Trait to mutate a value for test purposes",
            Fn("mutate_value", "Change the value in some deterministic way", refMutSelf),
        )

        val testSupportModule = Module(
            "support",
            "Support for db tests",
            statics = listOf(
                Static(
                    "test_db_pool", """The pg connection pool for tests""",
                    "tokio::sync::OnceCell<Pool>".asType,
                    StaticValue("tokio::sync::OnceCell::const_new()"),
                    attrs = attrCfgTest.asAttrList
                )
            ),
            traits = listOf(mutateValueTrait),
            traitImpls = listOf(
                "i64", "u64", "u32", "i32", "i16"
            ).map {
                listOf(
                    TraitImpl(
                        it.asType, mutateValueTrait,
                        bodies = mapOf("mutate_value" to "*self = self.add(1);")
                    ),
                    TraitImpl(
                        "Option<${it}>".asType, mutateValueTrait,
                        bodies = mapOf("mutate_value" to "self.as_mut().map(|v| v.add(1));")
                    ),
                )
            }.flatten() +
                    listOf(
                        TraitImpl(
                            RustString, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "self.push_str(\"*\");")
                        ),
                        TraitImpl(
                            "Option<String>".asType, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "self.as_mut().map(|v| v.push_str(\"*\"));")
                        ),
                        TraitImpl(
                            "Value".asType, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "// Need easy mutation")
                        ),
                        TraitImpl(
                            "Option<Value>".asType, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "// Need easy way to mutate")
                        ),
                        TraitImpl(
                            RustChar, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "*self = (*self as u8 + 1) as char;")
                        ),
                        TraitImpl(
                            "Option<char>".asType, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "self.as_mut().map(|c| *c = (*c as u8 + 1) as char);")
                        ),
                        TraitImpl(
                            RustBoolean, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "*self = !*self;")
                        ),
                        TraitImpl(
                            "Option<bool>".asType, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "self.as_mut().map(|v| *v = !*v);")
                        ),
                        TraitImpl(
                            "NaiveDate".asType, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "*self = *self + Duration::days(1);")
                        ),
                        TraitImpl(
                            "Option<NaiveDate>".asType, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "self.as_mut().map(|v| *v = *v + Duration::days(1));")
                        ),
                        TraitImpl(
                            "NaiveDateTime".asType, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "*self = *self + Duration::days(1);")
                        ),
                        TraitImpl(
                            "Option<NaiveDateTime>".asType, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "self.as_mut().map(|v| *v = *v + Duration::days(1));")
                        ),
                        TraitImpl(
                            "Uuid".asType, mutateValueTrait,
                            bodies = mapOf(
                                "mutate_value" to """
let bytes = self.as_bytes();
let namespace = Uuid::new_v5(&Uuid::NAMESPACE_DNS, b"kgen-test");
*self = Uuid::new_v5(&namespace, bytes)
                    """.trimIndent()
                            )
                        ),
                        TraitImpl(
                            "Option<Uuid>".asType, mutateValueTrait,
                            bodies = mapOf("mutate_value" to "self.as_mut().map(|u| u.mutate_value());")
                        )
                    ),
            functions = listOf(
                Fn(
                    "get_pool",
                    "Return the associated static connection pool",
                    body = FnBody(
                        "TEST_DB_POOL.get_or_init(initialize_db_pool).await.clone()"
                    ),
                    returnType = "Pool".asType,
                    returnDoc = "The pool",
                    inlineDecl = InlineDecl.Inline,
                    isAsync = true,
                ),
                Fn(
                    "initialize_db_pool",
                    "Initialize the pool connection - called once by `get_or_init`",
                    returnType = "Pool".asType,
                    returnDoc = "The client _singleton_",
                    body = FnBody(
                        """
    let mut cfg = Config::new();
    cfg.dbname = Some("kgen".to_string());
    cfg.user = Some("kgen".to_string());
    cfg.password = Some("kgen".to_string());
    cfg.manager = Some(ManagerConfig {
        recycling_method: RecyclingMethod::Fast,
    });
    cfg.create_pool(Some(Runtime::Tokio1), NoTls).unwrap()
    """.trimIndent()
                    ),
                    isAsync = true
                )
            ),
            uses = listOf(
                "deadpool_postgres::Config",
                "deadpool_postgres::ManagerConfig",
                "deadpool_postgres::Pool",
                "deadpool_postgres::Runtime",
                "deadpool_postgres::RecyclingMethod",
                "chrono::Duration",
                "chrono::NaiveDate",
                "chrono::NaiveDateTime",
                "serde_json::Value",
                "std::ops::Add",
                "tokio_postgres::NoTls",
                "uuid::Uuid",
            ).asUses
        )
    }
}

/** Various rust type ids and types involved in supporting the CRUD functions.
 * @property rowDataId The id associated with the primary **RowData** type
 * @property rowEntryId The id associated with the secondary **RowEntry** type which
 * breaks any _auto id_ out from the data column fields.
 * @property selectAllReturnType Return type for a _select all_/_select where_ query
 * @property bulkReturnType Return type for a _bulk insert_ or _bulk upsert_ query
 */
data class GatewayTypes(
    val rowDataId: Id,
    val rowEntryId: Id? = null,
    val selectAllReturnType: Type,
    val bulkReturnType: Type,
) {
    companion object {
        fun fromClassifier(id: Id, tableClassifier: DbTableClassifier) = when (tableClassifier) {
            DbTableClassifier.AutoId, DbTableClassifier.AutoIdWithPkey -> GatewayTypes(
                "${id}_row_data".asId,
                "${id}_row_entry".asId,
                "Vec<${id.capCamel}RowEntry>".asType,
                "Vec<${id.capCamel}RowEntry>".asType,
            )

            DbTableClassifier.Pkey, DbTableClassifier.Keyless -> GatewayTypes(
                "${id}_row_data".asId,
                null,
                "Vec<${id.capCamel}RowData>".asType,
                "Vec<${id.capCamel}RowData>".asType,
            )
        }
    }
}