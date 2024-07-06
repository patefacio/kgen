package kgen.rust.db

import kgen.db.asModeledTable
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
    }

    val modeledTable = TableSample.asModeledTable()
    println(modeledTable)

    println(TableGatewayGenerator(modeledTable).asModule.asRust)
}