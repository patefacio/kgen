package kgen.rust.generator

import kgen.*
import kgen.proto.*
import kgen.proto.Enum
import kgen.proto.Field
import kgen.rust.*
import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

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
 * @property customImplProtoPaths List of proto paths of messages or enums in
 * the proto file that require custom impls. Attaching an impl directly to a
 * protobuf message's struct is a convenient way to attach functionality to
 * the struct.
 * @property additionalDerives Set mapping Rust Message/Enum name to list of
 * additional derives.
 * @property includeRequiredValidators If set a module for validating field
 * presence will be generated
 */
data class ProtoCrateGenerator(
    val crateNameId: String,
    val doc: String = missingDoc(crateNameId, "Proto Crate"),
    val protoFiles: List<ProtoFile>,
    val targetProtoPath: Path,
    val targetCratePath: Path,
    val customImplProtoPaths: Map<String, List<Fn>> = emptyMap(),
    val additionalDerives: Map<String, Set<String>> = emptyMap(),
    val includeRequiredValidators: Boolean = false
) : Identifier(crateNameId) {

    private fun generateProtos() = protoFiles.map {
        it.generate(targetProtoPath)
    }

    /**
     * Definition of the rust module that will contain all messages requiring validation
     * with their generated validation functions.
     */
    private val requiredFieldsPresentTrait = Trait(
        "required_fields_present",
        "Method to check fields that are required (which is not supported by proto3) are present",
        Fn(
            "required_fields_present",
            "Checks all fields of type Message that are not specifically `optional` to ensure they are present.",
            refSelf,
            returnDoc = "True if all fields present (recursively).",
            returnType = RustBoolean
        ),
        visibility = Visibility.Pub
    )

    /**
     * Messages referenced as field types in the proto file are specifed with NamedType:
     * e.g. (some_proto.SomeMessage). This function gets at the message type name. A current
     * limitation of this setup is scoping is not strong and within a set of protobuf files
     * having the same named message can be a problem.
     */
    private fun typeName(n: String) = n.split(".").last()

    /**
     * Map of {NamedType -> Udt { a Message or Enum }}
     */
    private val udtsByNamedType = protoFiles.udtsByNamedType

    /**
     * Map of {MessageName -> Udt { A Message or Enum }}
     */
    private val udtsByName = udtsByNamedType.entries.associate { typeName(it.key) to it.value }

    /**
     * Map of all messages requiring validation. Key is MessageName
     */
    private val messagesRequiringValidation: MutableMap<String, Boolean> = mutableMapOf()

    private fun messageShouldValidate(namedType: String): Boolean {
        val udtName = typeName(namedType)
        val udt = udtsByName.get(udtName)!!
        val result = messagesRequiringValidation.get(udtName) ?: (udt is Message) && (udt as Message)
            .fields
            .filterIsInstance<Field>()
            // Skip optional because those are intended
            .filter { !it.isOptional }
            .any { field: Field ->
                val type = field.type
                when (type) {
                    is FieldType.NamedType -> {
                        val fieldUdt = udtsByName.get(typeName(type.name))!!
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
     * Proto3 no longer supports required and by design all fields are optional.
     * This can be painful for complex structures where you want assurances
     * that required data is present. So [includeRequiredValidators] will turn
     * that concept over and assume if it is not marked `optional` it is required.
     * Optional in Proto3 is there to give you an ability to check presence of a field.
     *
     * This function looks at the type of field and returns true if the *field type*
     * requires validation. If the field itself is required - that is checked with
     * `is_some()` and a further check on the found instance is run.
     */
    private fun fieldTypeShouldValidate(field: MessageField) = field is Field && when (field.type) {
        is FieldType.NamedType -> messagesRequiringValidation.contains(typeName(field.type.name))
        else -> false
    }

    /**
     * Defines the module with the required field validation code.
     */
    private val requiredValidatorsModule
        get() = Module(
            "required_fields_present",
            """
                Eases the pain of dealing with `proto3` choice to not support required fields.
                
                From a developer perspective it is nice to know the messages coming over have
                the fields set. However, since `proto3` assumes all items are _optional_ the 
                generated rust is littered with `Option<MessageType>` and there are lots of 
                checks required. It is rust, so there is no way around doing all the checks
                on access. But this supports the upfront check that all fields that are truly
                _required_ are present. In `proto3` **all** non-primitive fields are _optional_.
                But there was a problem in usage with that because some wanted really **optional**
                fields with the support of knowing the _presence_ of a field. So, in `proto3`
                a field with an _optional_ annotation is optional like all the others, but also
                supports the concept of checking for presence. 
                
                To support this _required_fields_present_ we ignore the intent of `proto3` and
                assume _all fields are **required** unless marked **optional**_. This does not
                change the generated files but gives an up-front way to validate messages from 
                the client.
            """.trimIndent(),

            traits = listOf(
            requiredFieldsPresentTrait
        ), traitImpls = udtsByNamedType
            .filter { (namedType, message) ->
                (message is Message) &&
                        messagesRequiringValidation.get(typeName(namedType)) ?: false
            }.mapNotNull { (namedType, udt) ->

                val message = udt as Message
                val rustName = namedType.replace(".", "::")
                val validatingFields = message.fields.filter { fieldTypeShouldValidate(it) }
                val body = validatingFields.mapNotNull { messageField ->
                    val field = messageField as Field
                    when {
                        field.isOptional -> null
                        else -> when (field.type) {
                            is FieldType.NamedType -> {
                                val fieldTypeName = typeName(field.type.name)
                                val fieldTypeRequiresCheck =
                                    messagesRequiringValidation.get(fieldTypeName) ?: false
                                val fieldUdt = udtsByName.get(fieldTypeName)!!

                                when {
                                    // Enums need no check
                                    fieldUdt is Enum -> null
                                    field.isRepeated -> if (fieldTypeRequiresCheck) {
                                        "self.${field.nameId}.iter().all(|${field.nameId}| ${field.nameId}.required_fields_present())"
                                    } else {
                                        null
                                    }

                                    else -> if (fieldTypeRequiresCheck) {
                                        "self.${field.nameId}.as_ref().map(|${field.nameId}| ${field.nameId}.required_fields_present()).unwrap_or(false)"
                                    } else {
                                        "self.${field.nameId}.is_some()"
                                    }
                                }
                            }

                            else -> null
                        }
                    }
                }.joinToString("&&\n")

                if (body.isNotEmpty()) {
                    TraitImpl(
                        message.id.capCamel.asType,
                        requiredFieldsPresentTrait,
                        uses = setOf(Use("crate::$rustName")),
                        bodies = mapOf(
                            "required_fields_present" to body
                        )
                    )
                } else {
                    null
                }
            },
            visibility = Visibility.Pub
        )

    fun generate(): List<MergeResult> {
        val generatedProtos = generateProtos()
        val protoFileNames = protoFiles.map { it.protoFileName }
        val customImplModules = if (customImplProtoPaths.isNotEmpty()) {

            fun itemName(rustPath: String) = rustPath.split("::").last()

            listOfNotNull(
                Module("custom_impls",
                    "Hand crafted impls for proto messages/enums",
                    moduleType = ModuleType.Directory,
                    modules = customImplProtoPaths.map { (itemPath, implFunctions) ->
                        val itemNameAsSnake = itemName(itemPath).asSnake
                        Module(
                            "${itemNameAsSnake}_impl",
                            "Hand-coded impl for $itemPath",
                            uses = uses(itemPath) + implFunctions.map { it.uses }.flatten(),
                            typeImpls = listOf(
                                TypeImpl(
                                    id(itemNameAsSnake).capCamel.asType, functions = implFunctions
                                )
                            )
                        )
                    }),
            )
        } else {
            emptyList()
        }

        val validationModules = if (includeRequiredValidators) {
            listOf(requiredValidatorsModule)
        } else {
            emptyList()
        }

        val serdeSerializables =
            protoFiles.map { it.messages }.flatten().map { it.id.capCamel } + protoFiles.map { it.enums }.flatten()
                .map { it.id.capCamel } + protoFiles.map { it.allOneOfs.map { it.nameId } }.flatten()

        val crate = Crate(crateNameId,
            doc,
            Module("lib", "Top library module for crate $crateNameId\n\n$doc", modules = protoFiles.map {
                Module(
                    it.nameId,
                    "Placeholder for ${it.nameId} proto",
                    moduleType = ModuleType.PlaceholderModule,
                    visibility = Visibility.Pub
                )
            } + customImplModules + validationModules, macroUses = listOf("serde_derive")),
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
                                        serdeSerializables.joinToString("\n") {
                                            val derives = (setOf(
                                                "Serialize", "Deserialize"
                                            ) + (additionalDerives[it] ?: emptySet())).joinToString()
                                            ".type_attribute(${doubleQuote(it)}, \"#[derive($derives)]\")"
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
                crateNameId, dependencies = listOf(
                    "tonic = \"0.8\"",
                    "prost = \"0.11\"",
                    "serde = \"^1.0.27\"",
                    "serde_derive = \"^1.0.27\"",
                    """tokio = { version = "1.0", features = ["macros", "rt-multi-thread"] }""",
                ), buildDependencies = listOf("""tonic-build = "0.8"""")
            )
        )

        return generatedProtos + CrateGenerator(crate, targetCratePath.pathString).generate()
    }
}