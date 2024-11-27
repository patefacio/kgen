package kgen.db

enum class DbTableClassifier {
    AutoId,
    AutoIdWithPkey,
    Pkey,
    Keyless;

    /** Returns true if table has _auto id */
    val hasAutoId get() = this == AutoId || this == AutoIdWithPkey
}