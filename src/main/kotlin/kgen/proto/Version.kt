package kgen.proto

enum class Version {
    Proto3;

    val asProto
        get() = when (this) {
            Proto3 -> "proto3"
        }
}