package kgen.proto

import kgen.Identifiable

interface MessageField : Identifiable, AsProto {
    val isNumbered: Boolean

    val numFields: Int
    fun copyFromNumber(number: Int): MessageField
}