package kgen.rust.db

import kgen.db.asDbTable
import kgen.meta.MetaPaths
import kgen.rust.Crate
import kgen.rust.Module
import kgen.rust.ModuleRootType
import kgen.rust.generator.CrateGenerator
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

object KgenDatabase {
    val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/kgen",
        driver = "org.postgresql.Driver",
        user = "kgen",
        password = "kgen"
    )
}

object TableSampleWithId : Table("sample_with_id") {
    val id = integer("auto_id").autoIncrement()
    val name = varchar("the_name", 255)
    val smallInt = short("the_small_int")
    val largeInt = long("the_large_int")
    val general_int = integer("general_int")
    val date = date("the_date")
    val dateTime = datetime("the_date_time")
    val uuid = uuid("the_uuid")
    val ulong = ulong("the_ulong")

    override val primaryKey = PrimaryKey(id)
}

object TableSample: Table("sample") {
    val name = varchar("the_name", 255)
    val smallInt = short("the_small_int")
    val largeInt = long("the_large_int")
    val general_int = integer("general_int")
    val date = date("the_date")
    val dateTime = datetime("the_date_time")
    val uuid = uuid("the_uuid")
    val ulong = ulong("the_ulong")

    override val primaryKey = PrimaryKey(name, smallInt)
}

object TableKeyless: Table("keyless") {
    val name = varchar("the_name", 255)
    val smallInt = short("the_small_int")
    val largeInt = long("the_large_int")
    val general_int = integer("general_int")
    val date = date("the_date")
    val dateTime = datetime("the_date_time")
    val uuid = uuid("the_uuid")
    val ulong = ulong("the_ulong")
}



fun main() {
    /*
    CREATE USER kgen WITH PASSWORD 'kgen';
    CREATE DATABASE kgen;
    GRANT ALL PRIVILEGES ON DATABASE kgen TO kgen;
     */
    KgenDatabase.database

    val tables = listOf(TableKeyless, TableSample, TableSampleWithId)

    transaction {
        addLogger(StdOutSqlLogger)
        tables.forEach { table -> SchemaUtils.drop(table) }
        tables.forEach { table -> SchemaUtils.create(table) }

        val dbTables = tables.map { it.asDbTable }

        val libModule = Module(
            "lib",
            moduleRootType = ModuleRootType.LibraryRoot,
            modules = dbTables.map { TableGatewayGenerator(it).asModule }
        )
        val targetPath = MetaPaths.tempPath.resolve("kgen_db")
        val crateGenerator = CrateGenerator(
            Crate("kgen_db", rootModule = libModule),
            targetPath.toString()
        )

        crateGenerator.generate(true)
    }
    //println(TableGatewayGenerator(modeledTable).asModule.asRust)
}