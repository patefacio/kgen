package kgen.proto

data class ProtoFileSet(val protoFiles: Set<ProtoFile>) {

    companion object {
        fun typeName(n: String) = n.split(".").last()
    }

    val allMessages = protoFiles.map { it.allMessages.flatten() }.flatten()

    val allFileEnums = protoFiles.map { protoFile ->
        protoFile.enums.map { enum -> Pair(protoFile, enum) }
    }.flatten()

    val allFileMessages = protoFiles.map { protoFile ->
        protoFile.allMessages.map { messages ->
            messages.map { message ->
                Pair(
                    protoFile,
                    message
                )
            }
        }.flatten()
    }.flatten()

    val udtsByNamedType = allFileMessages.associate { (protoFile, message) ->
        "${protoFile.nameId}.${message.id.capCamel}" to message as Udt
    } + allFileEnums.associate { (protoFile, enum) ->
        "${protoFile.nameId}.${enum.id.capCamel}" to enum as Udt
    }

    val udtsByName = udtsByNamedType.entries.associate { typeName(it.key) to it.value }

    fun getByName(name: String) = udtsByName.getValue(typeName(name))

}