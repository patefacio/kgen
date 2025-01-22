package kgen.rust.db

import kgen.db.intoDbTable
import kgen.meta.MetaPaths
import kgen.rust.*
import kgen.rust.generator.CrateGenerator
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PGobject

object KgenDatabase {
    val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/kgen",
        driver = "org.postgresql.Driver",
        user = "kgen",
        password = "kgen"
    )
}


class JsonBColumnType<T : Any> : ColumnType<T>() {
    override fun sqlType(): String = "JSONB"
    override fun valueFromDB(value: Any): T {
        return when (value) {
            is PGobject -> value.value as T
            is String -> value as T
            else -> throw IllegalArgumentException("Unexpected value type: ${value::class.java}")
        }
    }
}

class JsonColumnType<T : Any> : ColumnType<T>() {
    override fun sqlType(): String = "JSON"
    override fun valueFromDB(value: Any): T {
        return when (value) {
            is PGobject -> value.value as T
            is String -> value as T
            else -> throw IllegalArgumentException("Unexpected value type: ${value::class.java}")
        }
    }
}

object TableSampleWithId : Table("sample_with_id") {
    val id = integer("auto_id").autoIncrement()
    val name = varchar("the_name", 255)
    val boolean = bool("the_boolean")
    val smallInt = short("the_small_int")
    val largeInt = long("the_large_int")
    val bigInt = ulong("the_big_int")
    val date = date("the_date")
    val general_int = integer("the_general_int")
    val dateTime = datetime("the_date_time")
    val uuid = uuid("the_uuid")
    val ulong = ulong("the_ulong")
    val json = registerColumn<Any>("the_json", JsonColumnType())
    val jsonb = registerColumn<Any>("the_jsonb", JsonBColumnType())

    val nullableName = varchar("nullable_name", 255).nullable()
    val nullableBoolean = bool("nullable_boolean").nullable()
    val nullableSmallInt = short("nullable_small_int").nullable()
    val nullableLargeInt = long("nullable_large_int").nullable()
    val nullableBigInt = ulong("nullable_big_int").nullable()
    val nullableDate = date("nullable_date").nullable()
    val nullableGeneralInt = integer("nullable_general_int").nullable()
    val nullableDateTime = datetime("nullable_date_time").nullable()
    val nullableUuid = uuid("nullable_uuid").nullable()
    val nullableUlong = ulong("nullable_ulong").nullable()
    val nullableJson = registerColumn<Any>("nullable_json", JsonColumnType()).nullable()
    val nullableJsonb = registerColumn<Any>("nullable_jsonb", JsonBColumnType()).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
       uniqueIndex(name, smallInt)
    }
}

object TableSample : Table("sample") {
    val name = varchar("the_name", 255)
    val smallInt = short("the_small_int")
    val largeInt = long("the_large_int")
    val bigInt = ulong("the_big_int")
    val date = date("the_date")
    val general_int = integer("the_general_int")
    val dateTime = datetime("the_date_time")
    val uuid = uuid("the_uuid")
    val ulong = ulong("the_ulong")
    val json = registerColumn<Any>("the_json", JsonColumnType())
    val jsonb = registerColumn<Any>("the_jsonb", JsonBColumnType())

    val nullableName = varchar("nullable_name", 255).nullable()
    val nullableSmallInt = short("nullable_small_int").nullable()
    val nullableLargeInt = long("nullable_large_int").nullable()
    val nullableBigInt = ulong("nullable_big_int").nullable()
    val nullableDate = date("nullable_date").nullable()
    val nullableGeneralInt = integer("nullable_general_int").nullable()
    val nullableDateTime = datetime("nullable_date_time").nullable()
    val nullableUuid = uuid("nullable_uuid").nullable()
    val nullableUlong = ulong("nullable_ulong").nullable()
    val nullableJson = registerColumn<Any>("nullable_json", JsonColumnType()).nullable()
    val nullableJsonb = registerColumn<Any>("nullable_jsonb", JsonBColumnType()).nullable()

    override val primaryKey = PrimaryKey(name, smallInt)
}

object TableKeyless : Table("keyless") {
    val name = varchar("the_name", 255)
    val smallInt = short("the_small_int")
    val largeInt = long("the_large_int")
    val bigInt = ulong("the_big_int")
    val date = date("the_date")
    val general_int = integer("the_general_int")
    val dateTime = datetime("the_date_time")
    val uuid = uuid("the_uuid")
    val ulong = ulong("the_ulong")
    val json = registerColumn<Any>("the_json", JsonColumnType())
    val jsonb = registerColumn<Any>("the_jsonb", JsonBColumnType())

    val nullableName = varchar("nullable_name", 255).nullable()
    val nullableSmallInt = short("nullable_small_int").nullable()
    val nullableLargeInt = long("nullable_large_int").nullable()
    val nullableBigInt = ulong("nullable_big_int").nullable()
    val nullableDate = date("nullable_date").nullable()
    val nullableGeneralInt = integer("nullable_general_int").nullable()
    val nullableDateTime = datetime("nullable_date_time").nullable()
    val nullableUuid = uuid("nullable_uuid").nullable()
    val nullableUlong = ulong("nullable_ulong").nullable()
    val nullableJson = registerColumn<Any>("nullable_json", JsonColumnType()).nullable()
    val nullableJsonb = registerColumn<Any>("nullable_jsonb", JsonBColumnType()).nullable()
}


fun main() {
    /*
    CREATE USER kgen WITH PASSWORD 'kgen';
    CREATE DATABASE kgen;
    GRANT ALL PRIVILEGES ON DATABASE kgen TO kgen;
     */
    KgenDatabase.database

    val tables = mapOf(
        TableKeyless to "Table with no primary key or auto id",
        TableSample to "Table with primary key",
        TableSampleWithId to "Table with auto-id"
    )

    transaction {
        addLogger(StdOutSqlLogger)
        tables.keys.forEach { table -> SchemaUtils.drop(table) }
        tables.keys.forEach { table -> SchemaUtils.create(table) }

        val dbTables = tables.entries.map { (table, doc) -> table.intoDbTable(doc)  }
        val tableGateways = dbTables.map { TableGateway(it) }

        val libModule = Module(
            "lib",
            moduleRootType = ModuleRootType.LibraryRoot,
            modules = tableGateways.map { it.asModule },
        )

        val targetPath = MetaPaths.tempPath.resolve("kgen_db")
        val crateGenerator = CrateGenerator(
            Crate(
                "kgen_db",
                rootModule = libModule,
                integrationTestModules = listOf(
                    Module("db", "Pulls in the db tests", customModDecls = listOf(ModDecl("db_tests"))),
                    Module(
                        "db_tests",
                        "Tests for generated db code",
                        ModuleType.Directory,
                        modules = tableGateways.map { it.testModule } +
                                TableGateway.testSupportModule
                    )
                )
            ),
            targetPath.toString()
        )

        crateGenerator.generate(true)
    }
    //println(TableGatewayGenerator(modeledTable).asModule.asRust)
}