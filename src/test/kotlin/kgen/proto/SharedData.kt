package kgen.proto

import kgen.proto.FieldType.*

val fullNameMessage = Message(
    "full_name", "Full name of person",
    Field("first_name", ProtoString, "First name", 1),
    Field("second_name", ProtoString, "Second name", 2)
)

val colorEnum = kgen.proto.Enum(
    "color_choice",
    "Choose among available colors",
    EnumField("red", "The color red", 0),
    EnumField("green", "The color green", 1),
    EnumField("blue", "The color blue", 2)

)

val kitchenSink = Message(
    "kitchen_sink",
    "Kitchen sink message",
    Field("a_field", ProtoString, "A field", 1),
    Field("color_choices", EnumType(colorEnum), "Choices", 2, isRepeated = true),
    Field("full_names", NamedType("FullName"), "Messages", 3, isRepeated = true),
    messages = listOf(fullNameMessage),
    enums = listOf(colorEnum)
)

