package kgen.proto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MessageTest {

    @Test
    fun getAsProto() {

        assertEquals(
            """/*
  Full name of person
*/
message FullName {
  /*
    First name
  */
  string first_name = 1;
  
  /*
    Second name
  */
  string second_name = 2;
}""",
            fullNameMessage.asProto
        )

        assertEquals(
            """/*
  Kitchen sink message
*/
message KitchenSink {
  /*
    A field
  */
  string a_field = 1;
  
  /*
    Choices
  */
  repeated ColorChoice color_choices = 2;
  
  /*
    Messages
  */
  repeated FullName full_names = 3;
  
  /*
    Full name of person
  */
  message FullName {
    /*
      First name
    */
    string first_name = 1;
    
    /*
      Second name
    */
    string second_name = 2;
  };
  
  /*
    Choose among available colors
  */
  enum ColorChoice {
    /*
      The color red
    */
    RED = 0;
    
    /*
      The color green
    */
    GREEN = 1;
    
    /*
      The color blue
    */
    BLUE = 2;
  };
}""",
            kitchenSink.asProto
        )

    }
}