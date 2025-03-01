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

    data object Bool : DbType()

    data class BinarySized(val length: Int) : DbType()

    data object Blob : DbType()

    data object JsonBinary : DbType()

    data object Json : DbType()

    data class VarChar(val length: Int) : DbType()

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

    data object NullableBool : DbType()

    data object NullableBinary : DbType()

    data class NullableBinarySized(val length: Int) : DbType()

    data object NullableBlob : DbType()

    data object NullableJsonBinary : DbType()

    data object NullableJson : DbType()

    data class NullableVarChar(val length: Int) : DbType()


    val asRustType
        get() = when (this) {
            is DbType.Byte -> U8
            is Double -> F64
            is Integer -> I32
            is SmallInteger -> I16
            is BigInteger -> I64
            is Text -> RustString
            is Date -> "chrono::NaiveDate".asType
            is DateTime, is Timestamp -> "chrono::NaiveDateTime".asType
            is IntegerAutoInc -> I32
            is LongAutoInc -> I64
            is UlongAutoInc -> U64
            is Binary, is BinarySized -> I64
            is Bool -> RustBoolean
            is Uuid -> "uuid::Uuid".asType
            is VarChar -> RustString
            is Json, is JsonBinary -> "serde_json::Value".asType


            is NullableByte -> "Option<u8>".asType
            is NullableDouble -> "Option<f64>".asType
            is NullableInteger -> "Option<i32>".asType
            is NullableSmallInteger -> "Option<i16>".asType
            is NullableBigInteger -> "Option<i64>".asType
            is NullableText -> "Option<String>".asType
            is NullableDate -> "Option<chrono::NaiveDate>".asType
            is NullableDateTime, is NullableTimestamp -> "Option<chrono::NaiveDateTime>".asType
            is NullableBinary, is NullableBinarySized -> "Option<i64>".asType
            is NullableUuid -> "Option<uuid::Uuid>".asType
            is NullableBool -> "Option<bool>".asType
            is NullableVarChar -> "Option<String>".asType
            is NullableJson, is NullableJsonBinary -> "Option<serde_json::Value>".asType
            else -> throw (Exception("Unsupported rust type for $this"))
        }


    fun getSampleIterator() = when (this) {
        is DbType.Byte, is NullableByte -> generateSequence(KotlinByte.MIN_VALUE) {
            it.plus(1).toByte()
        }.iterator()

        is Double, is NullableDouble -> generateSequence(0.0) { it.plus(1.0) }.iterator()
        is Integer, is NullableInteger -> generateSequence(Int.MIN_VALUE) { it.plus(1) }.iterator()
        is SmallInteger, is NullableSmallInteger -> generateSequence(Short.MIN_VALUE) { (it + 1).toShort() }.iterator()
        is BigInteger, is NullableBigInteger -> generateSequence(Int.MIN_VALUE) { it.plus(1) }.iterator()
        is Bool, is NullableBool -> generateSequence(false) { it.not() }.iterator()
        is Text, is NullableText -> generateSequence("a") { incrementString(it) }.iterator()
        is Date, is NullableDate -> generateSequence(LocalDate.of(2000, 1, 1)) {
            it.plusDays(1).plusMonths(1).plusDays(1)
        }.iterator()

        is DateTime, is NullableDateTime -> generateSequence(LocalDateTime.of(2000, 1, 1, 1, 1)) {
            it.plusDays(1).plusMonths(1).plusDays(1)
        }.iterator()

        is IntegerAutoInc -> generateSequence(Int.MIN_VALUE) { it.plus(1) }.iterator()
        is LongAutoInc -> generateSequence(Long.MIN_VALUE) { it.plus(1) }.iterator()
        is UlongAutoInc -> generateSequence(ULong.MIN_VALUE) { it.plus(1.toULong()) }.iterator()
        //is DbType.Binary -> generateSequence(Long.MIN_VALUE) { it.plus(1) }.iterator()
        is Blob, is NullableBlob -> generateBlobSequence().iterator()
        is Uuid, is NullableUuid -> generateDeterministicUuidSequence().iterator()
        is Json, is NullableJson -> generateDeterministicJsonSequence().iterator()
        is VarChar, is NullableVarChar -> generateSequence("a") { incrementString(it) }.iterator()
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
