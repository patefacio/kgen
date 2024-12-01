package kgen.db

import kgen.rust.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random
import kotlin.Byte as KotlinByte

sealed class DbType {
    data object Byte : DbType()

    data object Double : DbType()

    data object Integer : DbType()

    data object SmallInteger : DbType()

    data object BigInteger : DbType()

    data object Text : DbType()

    data object Date : DbType()

    data object DateTime : DbType()

    data object Timestamp : DbType()

    data object Interval : DbType()

    data object IntegerAutoInc : DbType()

    data object LongAutoInc : DbType()

    data object UlongAutoInc : DbType()

    data object Uuid : DbType()

    data object Binary : DbType()

    data class BinarySized(val length: Int) : DbType()

    data object Blob : DbType()

    data object JsonBinary : DbType()

    data object Json : DbType()

    data class VarChar(val length: Int) : DbType();

    // Nullables

    data object NullableByte : DbType()

    data object NullableDouble : DbType()

    data object NullableInteger : DbType()

    data object NullableSmallInteger : DbType()

    data object NullableBigInteger : DbType()

    data object NullableText : DbType()

    data object NullableDate : DbType()

    data object NullableDateTime : DbType()

    data object NullableTimestamp : DbType()

    data object NullableInterval : DbType()

    data object NullableUuid : DbType()

    data object NullableBinary : DbType()

    data class NullableBinarySized(val length: Int) : DbType()

    data object NullableBlob : DbType()

    data object NullableJsonBinary : DbType()

    data object NullableJson : DbType()

    data class NullableVarChar(val length: Int) : DbType();


    val asRustType
        get() = when (this) {
            is DbType.Byte -> U8
            is DbType.Double -> F64
            is DbType.Integer -> I32
            is DbType.SmallInteger -> I16
            is DbType.BigInteger -> I64
            is DbType.Text -> RustString
            is DbType.Date -> "chrono::NaiveDate".asType
            is DbType.DateTime, is DbType.Timestamp -> "chrono::NaiveDateTime".asType
            is DbType.IntegerAutoInc -> I32
            is DbType.LongAutoInc -> I64
            is DbType.UlongAutoInc -> U64
            is DbType.Binary, is DbType.BinarySized -> I64
            is DbType.Uuid -> "uuid::Uuid".asType
            is DbType.VarChar -> RustString
            is DbType.Json, is DbType.JsonBinary -> "serde_json::Value".asType


            is DbType.NullableByte -> "Option<u8>".asType
            is DbType.NullableDouble -> "Option<f64".asType
            is DbType.NullableInteger -> "Option<i32>".asType
            is DbType.NullableSmallInteger -> "Option<i16>".asType
            is DbType.NullableBigInteger -> "Option<i64>".asType
            is DbType.NullableText -> "Option<String>".asType
            is DbType.NullableDate -> "Option<chrono::NaiveDate>".asType
            is DbType.NullableDateTime, is DbType.NullableTimestamp -> "Option<chrono::NaiveDateTime>".asType
            is DbType.NullableBinary, is DbType.NullableBinarySized -> "Option<i64>".asType
            is DbType.NullableUuid -> "Option<uuid::Uuid>".asType
            is DbType.NullableVarChar -> "Option<String>".asType
            is DbType.NullableJson, is DbType.NullableJsonBinary -> "Option<serde_json::Value>".asType
            else -> throw (Exception("Unsupported rust type for $this"))
        }


    fun getSampleIterator() = when (this) {
        is DbType.Byte, is DbType.NullableByte -> generateSequence(KotlinByte.MIN_VALUE) {
            it.plus(1).toByte()
        }.iterator()

        is DbType.Double, is DbType.NullableDouble -> generateSequence(0.0) { it.plus(1.0) }.iterator()
        is DbType.Integer, is DbType.NullableInteger -> generateSequence(Int.MIN_VALUE) { it.plus(1) }.iterator()
        is DbType.SmallInteger, is DbType.NullableSmallInteger -> generateSequence(Short.MIN_VALUE) { (it + 1).toShort() }.iterator()
        is DbType.BigInteger, is DbType.NullableBigInteger -> generateSequence(Int.MIN_VALUE) { it.plus(1) }.iterator()
        is DbType.Text, is DbType.NullableText -> generateSequence("a") { incrementString(it) }.iterator()
        is DbType.Date, is DbType.NullableDate -> generateSequence(LocalDate.of(2000, 1, 1)) {
            it.plusDays(1).plusMonths(1).plusDays(1)
        }.iterator()

        is DbType.DateTime, is DbType.NullableDateTime -> generateSequence(LocalDateTime.of(2000, 1, 1, 1, 1)) {
            it.plusDays(1).plusMonths(1).plusDays(1)
        }.iterator()

        is DbType.IntegerAutoInc -> generateSequence(Int.MIN_VALUE) { it.plus(1) }.iterator()
        is DbType.LongAutoInc -> generateSequence(Long.MIN_VALUE) { it.plus(1) }.iterator()
        is DbType.UlongAutoInc -> generateSequence(ULong.MIN_VALUE) { it.plus(1.toULong()) }.iterator()
        //is DbType.Binary -> generateSequence(Long.MIN_VALUE) { it.plus(1) }.iterator()
        is DbType.Blob, is DbType.NullableBlob -> generateBlobSequence().iterator()
        is DbType.Uuid, is DbType.NullableUuid -> generateDeterministicUuidSequence().iterator()
        is DbType.Json, is DbType.NullableJson -> generateDeterministicJsonSequence().iterator()
        is DbType.VarChar, is DbType.NullableVarChar -> generateSequence("a") { incrementString(it) }.iterator()
        else -> throw (Exception("Unsupported rust type for $this"))
    }
}

private fun incrementString(s: String): String {
    val lastChar = s.last()
    val rest = s.dropLast(1)
    return if (lastChar < 'z') {
        rest + (lastChar + 1)
    } else {
        incrementString(rest.ifEmpty { "a" }) + 'a'
    }
}

// Generates a deterministic sequence of UUIDs
fun generateDeterministicUuidSequence(): Sequence<UUID> {
    var counter = 0L // A counter for deterministic UUID generation
    val namespace = UUID.nameUUIDFromBytes("kgen".toByteArray())
    return generateSequence {
        val counterBytes = counter.toString().toByteArray()
        counter++
        UUID.nameUUIDFromBytes(namespace.toString().toByteArray() + counterBytes)
    }
}

// Generates a deterministic sequence of JSON data
fun generateDeterministicJsonSequence(): Sequence<String> {
    var counter = 0L // A counter for deterministic UUID generation
    return generateSequence {
        counter++
        "{ value: $counter }"
    }
}

// Generates a deterministic sequence of JSON data
fun generateDeterministicBlobSequence(): Sequence<String> {
    var counter = 0L // A counter for deterministic UUID generation
    return generateSequence {
        counter++
        "{ value: $counter }"
    }
}

// Generates deterministic binary data (e.g., for BYTEA)
fun generateBlobSequence(): Sequence<ByteArray> {
    val random = Random(42) // Seeded for reproducibility
    return generateSequence {
        ByteArray(16).apply { random.nextBytes(this) } // 16 bytes of random data
    }
}
