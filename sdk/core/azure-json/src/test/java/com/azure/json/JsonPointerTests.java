package com.azure.json;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonPointerTests {




    private JsonObject input = (JsonObject) new JsonBuilder().build(
              "{\"Array\": [1, 2, 3], \"\":\"Blank\", \"//\":\"Slash\", \"%%\": \"PCent\", \"^^\": \"Point\", \"||\": \"Line\", \"\\\\\\\\\": \"BSlash\", \" \": \"Space\", \"~~\":\"Squiggle\", \"Object\":{\"InnerObject\": [4096]}, \"~\": \"SQUIGGLE\", \"~x\":222}");

    public JsonPointerTests() throws IOException {
    }

    @Test
    public void pointerAll() throws IOException {
        JsonElement result = input.toJsonPointer("");
        assertEquals(input.toString(), result.toString());
    }

    @Test
    public void pointerObjectWithArray() throws IOException {
        JsonElement result = input.toJsonPointer("/Array");
        assertEquals("[1,2,3]", result.toString());
    }

    @Test
    public void pointerArrayEntryInObject() throws IOException {
        JsonElement result = input.toJsonPointer("/Array/1");
        assertEquals("2", result.toString()); //Need to alter toString for JsonStrings... Check if impacts other functions before doing so.
    }

    @Test
    public void blankEntry() throws IOException {
        JsonElement result = input.toJsonPointer("/");
        assertEquals("Blank", result.toString()); //Need to alter toString for JsonStrings... Check if impacts other functions before doing so.
    }

    @Test
    public void specialCharZero() throws IOException {
        JsonElement result = input.toJsonPointer("/~0~0");
        assertEquals("Squiggle", result.toString());
    }

    @Test
    public void specialCharOne() throws IOException {
        JsonElement result = input.toJsonPointer("/~1~1");
        assertEquals("Slash", result.toString());
    }

    @Test
    public void getInnerObject() throws IOException {
        JsonElement result = input.toJsonPointer("/Object/InnerObject/0");
        assertEquals("4096", result.toString());
    }

    @Test
    public void getSingleSquiggle() throws IOException {
        assertThrows(IOException.class, ()->input.toJsonPointer("/~"));
    }

    @Test
    public void getSquiggleChar() throws IOException {
        assertThrows(IOException.class, ()->input.toJsonPointer("/~x"));
    }

    @Test
    public void getSquiggleCharValid() throws IOException {
        JsonElement result = input.toJsonPointer("/~0x");
        assertEquals("222", result.toString());
    }
}
