package kgen.proto

import kgen.Identifiable

interface MessageField : Identifiable, AsProto {
    val isNumbered: Boolean
    fun copyFromNumber(number: Int): MessageField
}