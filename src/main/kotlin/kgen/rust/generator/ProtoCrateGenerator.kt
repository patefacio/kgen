package kgen.rust.generator

import kgen.*
import kgen.proto.ProtoFile
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
 */
data class ProtoCrateGenerator(
    val crateNameId: String,
    val doc: String = missingDoc(crateNameId, "Proto Crate"),
    val protoFiles: List<ProtoFile>,
    val targetProtoPath: Path,
    val targetCratePath: Path,
    val customImplProtoPaths: Map<String, List<Fn>> = emptyMap(),
    val additionalDerives: Map<String, Set<String>> = emptyMap()
) : Identifier(crateNameId) {

    private fun generateProtos() = protoFiles.map {
        it.generate(targetProtoPath)
    }

    fun generate(): List<MergeResult> {
        val generatedProtos = generateProtos()
        val protoFileNames = protoFiles.map { it.protoFileName }

        val customImplModules = if (customImplProtoPaths.isNotEmpty()) {

            fun itemName(rustPath: String) = rustPath.split("::").last()

            listOf(
                Module(
                    "custom_impls",
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
                                    id(itemNameAsSnake).capCamel.asType,
                                    functions = implFunctions
                                )
                            )
                        )
                    }
                )
            )
        } else {
            emptyList()
        }

        val serdeSerializables = protoFiles
            .map { it.messages }
            .flatten()
            .map { it.id.capCamel } +
                protoFiles
                    .map { it.enums }
                    .flatten()
                    .map { it.id.capCamel } +
                protoFiles
                    .map { it.allOneOfs.map { it.nameId } }
                    .flatten()

        val crate = Crate(
            crateNameId,
            doc,
            Module(
                "lib",
                "Top library module for crate $crateNameId\n\n$doc",
                modules = protoFiles.map {
                    Module(
                        it.nameId, "Placeholder for ${it.nameId} proto",
                        moduleType = ModuleType.PlaceholderModule,
                        visibility = Visibility.Pub
                    )
                } + customImplModules,
                macroUses = listOf("serde_derive")
            ),
            Module(
                "build",
                "Build proto files.",
                functions = listOf(
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
                                        serdeSerializables
                                            .joinToString("\n") {
                                                val derives = (setOf(
                                                    "Serialize",
                                                    "Deserialize"
                                                ) + (additionalDerives[it] ?: emptySet())).joinToString()
                                                ".type_attribute(${doubleQuote(it)}, \"#[derive($derives)]\")"
                                            },
                                        "    "
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
                dependencies = listOf(
                    "tonic = \"0.8\"",
                    "prost = \"0.11\"",
                    "serde = \"^1.0.27\"",
                    "serde_derive = \"^1.0.27\"",
                    """tokio = { version = "1.0", features = ["macros", "rt-multi-thread"] }""",
                ),
                buildDependencies = listOf("""tonic-build = "0.8"""")
            )
        )

        return generatedProtos + CrateGenerator(crate, targetCratePath.pathString).generate()
    }
}