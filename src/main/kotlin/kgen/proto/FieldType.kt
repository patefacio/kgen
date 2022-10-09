package kgen.proto

sealed class FieldType : AsProto {
    object ProtoString : FieldType() {
        override val asProto: String
            get() = "string"
    }

    object ProtoFloat : FieldType() {
        override val asProto: String
            get() = "float"
    }

    object ProtoDouble : FieldType() {
        override val asProto: String
            get() = "double"
    }

    object ProtoInt32 : FieldType() {
        override val asProto: String
            get() = "int32"
    }

    object ProtoInt64 : FieldType() {
        override val asProto: String
            get() = "int64"
    }

    object ProtoUInt32 : FieldType() {
        override val asProto: String
            get() = "uint32"
    }

    object ProtoUInt64 : FieldType() {
        override val asProto: String
            get() = "uint64"
    }

    object ProtoSInt32 : FieldType() {
        override val asProto: String
            get() = "sint32"
    }

    object ProtoSInt64 : FieldType() {
        override val asProto: String
            get() = "sint64"
    }

    object ProtoFixed32 : FieldType() {
        override val asProto: String
            get() = "fixed32"
    }

    object ProtoSFixed32 : FieldType() {
        override val asProto: String
            get() = "sfixed32"
    }

    object ProtoSFixed64 : FieldType() {
        override val asProto: String
            get() = "sfixed64"
    }

    object ProtoBoolean : FieldType() {
        override val asProto: String
            get() = "bool"
    }

    object ProtoBytes : FieldType() {
        override val asProto: String
            get() = "bytes"
    }

    class MessageType(val message: Message, val proto: String? = null) : FieldType() {
        override val asProto: String
            get() = if(proto != null) {
                "$proto."
            } else {
                ""
            } + message.id.capCamel
    }

    class EnumType(val enum: Enum) : FieldType() {
        override val asProto: String
            get() = enum.id.capCamel
    }

    class NamedType(val name: String) : FieldType() {
        override val asProto: String
            get() = name
    }

    class MapOf(val keyType: FieldType, val valueType: FieldType) : FieldType() {
        override val asProto: String
            get() = "map<${keyType.asProto}, ${valueType.asProto}>"
    }
}