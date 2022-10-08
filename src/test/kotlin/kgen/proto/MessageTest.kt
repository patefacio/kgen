package kgen.proto

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class MessageTest {

    @Test
    fun getAsProto() {

        assertEquals(
            """message FullName {
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
            """message KitchenSink {
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