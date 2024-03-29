package kgen.rust.generator

import kgen.*
import kgen.proto.*
import kgen.proto.Enum
import kgen.proto.Field
import kgen.rust.*
import java.nio.file.Path
import kotlin.io.path.*

/** Generates rust package from list of protobuf files.
 *
 * Uses tonic to generate rust. The strategy for tonic is to create
 * a build.rs file to incorporate generating the source in the build step.
 *
 * @property crateNameId Name of crate
 * @property doc Document for the crate
 * @property protoFiles List of protobuf files models to be transformed to .proto files
 * @property targetProtoPath Where generated proto files should be generated
 * @property targetCratePath Where crate should be generated
 * @property generatorDeletedPath Occasionally users may rename files and not
 * remember to remove the old named generated proto-file. If this is set, any
 * non-generated proto files will be moved to this path. Otherwise a warning
 * message will show.
 * @property customImplProtoPaths List of proto paths of messages or enums in
 * the proto file that require custom impls. Attaching an impl directly to a
 * protobuf message's struct is a convenient way to attach functionality to
 * the struct.
 * @property additionalDerives Set mapping Rust Message/Enum name to list of
 * additional derives.
 * @property additionalTraitImpls Map of message to list of trait impls. Suppose
 * you want to add a trait implementation for `AddAssign` to a message. In rust
 * you would have to wrap it, because the struct for the message will not be
 * part of the client crate using the proto generated files. Instead, pass the
 * traits you want to implement.
 */
data class ProtoCrateGenerator(
    val crateNameId: String,
    val doc: String = missingDoc(crateNameId, "Proto Crate"),
    val protoFiles: List<ProtoFile>,
    val targetProtoPath: Path,
    val targetCratePath: Path,
    val generatorDeletedPath: Path? = null,
    val customImplProtoPaths: Map<String, List<Fn>> = emptyMap(),
    val additionalDerives: Map<String, Set<String>> = emptyMap(),
    val additionalTraitImpls: Map<Message, List<TraitImpl>> = emptyMap(),
    val additionalMessageImpls: Map<Message, TypeImpl> = emptyMap(),
    val additionalEnumImpls: Map<Enum, TypeImpl> = emptyMap(),
    val uses: Set<Use> = emptySet(),
    val exportTypes: Boolean = false,
    val includeDisplayImpls: Boolean = false,
    val additionalModules: List<Module> = emptyList()
) : Identifier(crateNameId) {

    private fun generateProtos() = protoFiles.map {
        it.generate(targetProtoPath)
    }

    private fun checkDeleteUnwantedFile(path: Path) {
        if (generatorDeletedPath != null) {
            println("MOVING UNWANTED: $path to $generatorDeletedPath")
            generatorDeletedPath.createDirectories()
            path.moveTo(generatorDeletedPath.resolve(path.name))
        } else {
            println("WARNING: NON GENERATED proto FILE -> $path")
        }
    }

    /**
     * Messages referenced as field types in the proto file are specified with NamedType:
     * e.g. (some_proto.SomeMessage). This function gets at the message type name. A current
     * limitation of this setup is scoping is not strong and within a set of protobuf files
     * having the same named message can be a problem.
     */
    fun typeName(n: String) = n.split(".").last()

    /** Names of all the proto files.
     *
     */
    val protoFileNames = protoFiles.map { it.protoFileName }

    /**
     * Map of {NamedType -> Udt { a Message or Enum }}
     */
    val udtsByNamedType = protoFiles.udtsByNamedType

    /**
     * Map of {MessageName -> Udt { A Message or Enum }}
     */
    val udtsByName = udtsByNamedType.entries.associate { typeName(it.key) to it.value }

    /**
     * Map of all messages requiring validation. Key is MessageName
     */
    val messagesRequiringValidation: MutableMap<String, Boolean> = mutableMapOf()

    /**
     * Transform any trait impls to rust modules
     */
    private val additionalTraitImplModules = additionalTraitImpls.map { (message, traitImpls) ->
        Module("${message.nameId}_traits", null, traitImpls = traitImpls)
    }

    /**
     * Transform any message impl to a rust module
     */
    private val additionalMessageImplModules = additionalMessageImpls.map { (message, impl) ->
        Module(
            "${message.id}_impl",
            "An impl for message ${message.id}",
            typeImpls = listOf(impl)
        )
    }

    /**
     * Transform any message impl to a rust module
     */
    private val additionalEnumImplModules = additionalEnumImpls.map { (enum, impl) ->
        Module(
            "${enum.id}_impl",
            "An impl for enum ${enum.id}",
            typeImpls = listOf(impl)
        )
    }

    val allAdditionalModules = additionalModules +
            additionalTraitImplModules +
            additionalEnumImplModules +
            additionalMessageImplModules

    private fun messageShouldValidate(namedType: String): Boolean {
        val udtName = typeName(namedType)
        val udt = udtsByName[udtName]!!
        val result = messagesRequiringValidation[udtName] ?: (udt is Message) && (udt as Message)
            .fields
            .filterIsInstance<Field>()
            // Skip optional because those are intended
            .filter { !it.isOptional }
            .any { field: Field ->
                when (val type = field.type) {
                    is FieldType.NamedType -> {
                        val fieldUdt = udtsByName[typeName(type.name)]!!
                        if (field.isRepeated) {
                            messageShouldValidate(type.name)
                        } else {
                            // If enum no need to check
                            fieldUdt is Message
                        }
                    }

                    else -> false
                }
            }
        messagesRequiringValidation[udtName] = result
        return result
    }

    init {
        udtsByNamedType.forEach { (namedType, _) ->
            messageShouldValidate(namedType)
        }
    }

    /**
     * Proto3 no longer supports `required` and by design all fields are optional.
     * This can be painful for complex structures where you want assurances
     * that required data is present. This tracks such messages requiring validation.
     */
    fun fieldTypeShouldValidate(field: MessageField) = field is Field && when (field.type) {
        is FieldType.NamedType -> {
            val lookupName = typeName(field.type.name)
            messagesRequiringValidation[lookupName] ?: true
        }

        else -> false
    }

    private fun checkForNonGeneratedProtos(generatedProtos: List<MergeResult>) {
        val generatedSet = generatedProtos.map { mergeResult ->
            targetProtoPath.resolve(mergeResult.targetPath)
        }.toSet()

        val existingProtoFiles = targetProtoPath.toFile().walk().filter {
            it.path.endsWith(".proto")
        }.map { it.toPath() }

        existingProtoFiles.forEach { path ->
            if (!generatedSet.contains(path)) {
                checkDeleteUnwantedFile(path)
            }
        }
    }

    fun generate(): List<MergeResult> {
        val generatedProtos = generateProtos()
        checkForNonGeneratedProtos(generatedProtos)

        val enumSerializables = protoFiles.map { it.enums }.flatten()
            .map { it.id.capCamel }
            .toSet()

        val serdeSerializables =
            protoFiles.map { it.messages }.flatten()
                .map { it.id.capCamel } + enumSerializables + protoFiles.map { it.allOneOfs.map { it.nameId } }
                .flatten()

        val exportUsings = if (exportTypes) {
            udtsByNamedType.map { (k, _) -> "crate::$k".replace(".", "::").asPubUse }
        } else {
            emptyList()
        }

        fun makeTraitsModule(messages: List<Message>, trait: Trait, moduleName: String) =
            messages.filter { message -> message.implementedTraits.contains(trait) }
                .takeIf { it.isNotEmpty() }
                ?.map { message ->
                    TraitImpl(
                        message.id.capCamel.asType,
                        trait,
                        uses = listOf("crate::${message.id.capCamel}").asUses
                    )
                }
                ?.let {
                    Module(
                        moduleName,
                        "Implementation for ${trait.nameId.asId.capCamel} trait",
                        traitImpls = it,
                    )
                }

        val crate = Crate(
            crateNameId,
            doc,
            Module(
                "lib",
                "Top library module for crate $crateNameId\n\n$doc",
                moduleRootType = ModuleRootType.LibraryRoot,
                modules = protoFiles.map { protoFile ->
                    listOfNotNull(
                        Module(
                            protoFile.nameId,
                            "Placeholder for ${protoFile.nameId} proto",
                            moduleType = ModuleType.PlaceholderModule,
                            visibility = Visibility.Pub
                        ),
                        makeTraitsModule(protoFile.messages, displayTrait, "${protoFile.nameId}_display"),
                    )
                }.flatten() + allAdditionalModules,
                macroUses = listOf("serde_derive", "strum_macros"),
                uses = uses + exportUsings
            ),
            Module(
                "build", "Build proto files.", functions = listOf(
                    Fn(
                        "main",
                        "Incorporate protobuf to rust transformation into the build step.",
                        returnDoc = "Nothing or any errors",
                        returnType = "Result<(), Box<dyn std::error::Error>>".asType,
                        body = FnBody(
                            indent(
                                """
tonic_build::configure()
    .build_server(false)
    .out_dir("src/")
${
                                    indent(
                                        serdeSerializables.joinToString("\n") { typeName ->

                                            val derives = mutableSetOf("Serialize", "Deserialize")
                                            derives.addAll(additionalDerives[typeName] ?: emptySet())

                                            if (enumSerializables.contains(typeName)) {
                                                derives.addAll(listOf("EnumVariantNames", "EnumIter"))
                                            }

                                            val joinedDerives = derives.joinToString()

                                            ".type_attribute(${doubleQuote(typeName)}, \"#[derive($joinedDerives)]\")"
                                        }, "    "
                                    )
                                } 
    .compile(&[ 
${indent(protoFileNames.joinToString(",\n") { doubleQuote(it) }, "      ")}
    ], &["${targetProtoPath.relativeTo(targetCratePath)}"])?;
Ok(())
"""
                            )!!
                        )
                    )
                )
            ),
            cargoToml = CargoToml(
                crateNameId,
                description = doc,
                dependencies = listOf(
                    "prost = \"0.11\"",
                    "serde = \"^1.0.27\"",
                    "serde_derive = \"^1.0.27\"",
                ), buildDependencies = listOf("""tonic-build = "0.8"""")
            )
        )

        return generatedProtos + CrateGenerator(
            // The majority rust files in the proto generated directory are not generated directly.
            // Don't pass on the `generatorDeletedPath` to prevent deletion of tonic generated files.
            crate, targetCratePath.pathString, noWarnNonGenerated = true
        ).generate()
    }
}