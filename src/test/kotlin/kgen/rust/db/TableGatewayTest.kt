package kgen.rust.db

import kgen.db.asModeledTable
import kgen.meta.MetaPaths
import kgen.rust.Crate
import kgen.rust.Module
import kgen.rust.ModuleRootType
import kgen.rust.generator.CrateGenerator
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Paths

object KgenDatabase {
    val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/kgen",
        driver = "org.postgresql.Driver",
        user = "kgen",
        password = "kgen"
    )
}

object TableSample : Table("sample") {
    val id = integer("id").autoIncrement()
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

fun main() {
    /*
    CREATE USER kgen WITH PASSWORD 'kgen';
    CREATE DATABASE kgen;
    GRANT ALL PRIVILEGES ON DATABASE kgen TO kgen;
     */
    KgenDatabase.database

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.drop(TableSample)
        SchemaUtils.create(TableSample)


        val modeledTable = TableSample.asModeledTable()
        val libModule = Module(
            "lib",
            moduleRootType = ModuleRootType.LibraryRoot,
            modules = listOf(
                TableGatewayGenerator(modeledTable).asModule
            )
        )
        val targetPath = MetaPaths.tempPath.resolve("kgen_db")
        val crateGenerator = CrateGenerator(
            Crate("kgen_db", rootModule = libModule),
            targetPath.toString()
        )

        crateGenerator.generate(true)
    }
    //println(TableGatewayGenerator(modeledTable).asModule.asRust)
    println(TableGatewayGenerator(modeledTable).asModule.asRust)

    transaction {
        addLogger(StdOutSqlLogger)
        val conn = TransactionManager.current().connection
        val stmt = conn.prepareStatement("SELECT user", emptyArray())
        val x = stmt.executeQuery()
        x.next()
        println(x.getString("user"))
    }
}