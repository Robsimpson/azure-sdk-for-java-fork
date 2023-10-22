package com.azure.json;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonPathTests {

    JsonBuilder builder = new JsonBuilder();
    File fileA = new File("src/test/resources/JsonPathTestsA.json");
    String contentA = Files.readString(fileA.toPath());
    JsonElement sourceA = builder.build(contentA);

    File fileB = new File("src/test/resources/JsonPathTestsB.json");
    String contentB = Files.readString(fileB.toPath());
    JsonElement sourceB = builder.build(contentB);

    File fileC = new File("src/test/resources/JsonPathTestsC.json");
    String contentC = Files.readString(fileC.toPath());
    JsonElement sourceC = builder.build(contentC);

    File fileD = new File("src/test/resources/JsonPathTestsD.json");
    String contentD = Files.readString(fileD.toPath());
    JsonElement sourceD = builder.build(contentD);





    public JsonPathTests() throws IOException {
    }

    /*
    JsonPath functionality notes:
    - Functionality mostly based off the jsonpath online evaluator at jsonpath.com, though some differences exist
    - Original will not include keys if their value is JsonNull. Mine currently does.
    - My version requires spaces in scripts for simplicity, but this can be reworked.
    - For scripts, I have only ever seen usage of keyword length. Others can be added though.
    - Script only works with single equations. Example works with multiple (eg (@length-2+3*4), but I don't believe there is any reason to do so.
    - Filter only works in one direction. AKA @ = 5 works, but 5 = @ will not.
    - Currently do not have filter option 'subsetof', mostly because I don't fully understand it yet.
    - Based of the original, (@ != 5 && @ != 2 || @ == 5) evaluates like ((@ && @) || @)
    - Did not add (@.name == @.age) etc, as the original version seems buggy, and don't know if it should be replicated.
    - Example did not have an implementation of =~ regex to work with, so uses Java's .matches.
    - Example did not have implementation of contains, in, nin, empty true or empty false. Simply made assumptions
    - Example does not allow keys which have : in them, if they can be valid for an array. Ours allows this as it can check the element's type.
    - length command for array, object and string use keyword "length()".
    - With ..*,Keyword, the example places the keyword in the middle of all the text. Ours places it at the end. I would argue placing it after makes more sense.
    - For some reason, semicolon is treated as an instance of [. I have no idea why. That being said, perhaps we should not allow semicolons in the input for security reasons?
     - With filters, it is possible to have two ' symbols in front and still function. Ours only works with one, as I see no reason why it would allow this, or who would ever use it.
     - There is a feature in the example where adding ~ will return the keys of every match, and ignores everything past it. I did not add this feature since paths does something
     very similar. However, let us know if you want it added.
     - There is a feature in the example where adding ^ will return the previous layer of items. This was not added due to the paths feature, but
     let us know if you want this added.
     - The character ` also had some weird functionality in the example, which was not added. It is just treated as part of the key in this version.
     - There is a strange instance in the example where if script only contains the word 'length', it defaults to the fifth item? This was not included in our version
     - Though all values will be successfully obtained by ALL, ther order differs from the example sometimes. I believe this to be fine as the order is still logical.
     - Inputs with !@ all default to NO MATCHES, as there seems little reason to specify a value. If it doesn't have it, don't use it.

     */

    public String buildJsonPath(JsonElement source, String input, String args) throws IOException {
        if(source.isObject()){
            return ((JsonObject)source).toJsonPath(input, args);
        } else if (source.isArray()){
            return ((JsonArray)source).toJsonPath(input, args);
        } else {
            throw new IOException();
        }
    }

    @Test
    public void noInput() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceC,"", "VALUE");
        assertEquals("[\"Blank\"]", output);
    }

    @Test
    public void dollarOnly() throws IOException {
        String output = buildJsonPath(sourceA,"$", "VALUE");
        assertEquals("[{\"StringType\":\"String\",\"NumberType\":123,\"BooleanType\":true,\"NullType\":null,\"ObjectType\":{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},\"ArrayType\":[1,2,3,4,5,6,7],\"ObjectType2\":{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},\"ArrayType2\":[9,9,{\"InnerValue1\":\"Special\"}]}]", output);
    }

    @Test
    public void dollarDot() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceA,"$.", "VALUE");
        assertEquals("[{\"StringType\":\"String\",\"NumberType\":123,\"BooleanType\":true,\"NullType\":null,\"ObjectType\":{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},\"ArrayType\":[1,2,3,4,5,6,7],\"ObjectType2\":{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},\"ArrayType2\":[9,9,{\"InnerValue1\":\"Special\"}]}]", output);
    }

    @Test
    public void dollarDotQuote() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceA,"$.'", "VALUE");
        assertEquals("[{\"StringType\":\"String\",\"NumberType\":123,\"BooleanType\":true,\"NullType\":null,\"ObjectType\":{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},\"ArrayType\":[1,2,3,4,5,6,7],\"ObjectType2\":{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},\"ArrayType2\":[9,9,{\"InnerValue1\":\"Special\"}]}]", output);
    }

    @Test
    public void dollarDotQuoteDotQuote() throws IOException { //Should be read as ALL
        String output = buildJsonPath(sourceC,"$.'.'", "VALUE");
        assertEquals("[{\"empty object\":{},\"empty array\":[],\"empty word\":null,\"nameWith.Dot\":\"DOT\",\"backSlash\\\\\":\"BSlash\",\";\":\"Should not appear\",\"\":\"Blank\",\"[\":\"Bad Bracket Left\",\"]\":\"Bad Bracket Right\",\"0\":\"Zero\",\"$.\":\"DollarDot\",\"2:3:4\":\"Two Three Four\",\"1:1:1:1\":\"One One One One\",\"keyword\":\"Outside\",\"filledObject\":{\"keyword\":\"Inside1\"},\"filledobject\":{\"innerArray\":[1,3,{\"keyword\":\"Inside2\"}]}},{},[],null,{\"keyword\":\"Inside1\"},{\"innerArray\":[1,3,{\"keyword\":\"Inside2\"}]},[1,3,{\"keyword\":\"Inside2\"}],{\"keyword\":\"Inside2\"}]", output);
    }

    @Test
    public void dollarDotDoubleQuote() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceC,"$.''", "VALUE");
        assertEquals("[\"Blank\"]", output);
    }

    @Test
    public void dollarDotDoubleQuoteDot() throws IOException { //Should be read as ALL
        String output = buildJsonPath(sourceC,"$.''.", "VALUE");
        assertEquals("[{\"empty object\":{},\"empty array\":[],\"empty word\":null,\"nameWith.Dot\":\"DOT\",\"backSlash\\\\\":\"BSlash\",\";\":\"Should not appear\",\"\":\"Blank\",\"[\":\"Bad Bracket Left\",\"]\":\"Bad Bracket Right\",\"0\":\"Zero\",\"$.\":\"DollarDot\",\"2:3:4\":\"Two Three Four\",\"1:1:1:1\":\"One One One One\",\"keyword\":\"Outside\",\"filledObject\":{\"keyword\":\"Inside1\"},\"filledobject\":{\"innerArray\":[1,3,{\"keyword\":\"Inside2\"}]}},{},[],null,{\"keyword\":\"Inside1\"},{\"innerArray\":[1,3,{\"keyword\":\"Inside2\"}]},[1,3,{\"keyword\":\"Inside2\"}],{\"keyword\":\"Inside2\"}]", output);
    }

    @Test
    public void dollarSingleQuoteInBrackets() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceC,"$[']", "VALUE");
        assertEquals("[\"Blank\"]", output);
    }

    @Test
    public void dollarDoubleQuoteInBrackets() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceC,"$['']", "VALUE");
        assertEquals("[\"Blank\"]", output);
    }

    @Test
    public void dollarSemiColonEndBracket() throws IOException { //Semicolon acts like start bracket, so should return key "" instead of key "$;"
        String output = buildJsonPath(sourceC,"$;]", "VALUE");
        assertEquals("[\"Blank\"]", output);
    }


    @Test
    public void dollarBracket() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceA,"$[", "VALUE");
        assertEquals("[{\"StringType\":\"String\",\"NumberType\":123,\"BooleanType\":true,\"NullType\":null,\"ObjectType\":{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},\"ArrayType\":[1,2,3,4,5,6,7],\"ObjectType2\":{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},\"ArrayType2\":[9,9,{\"InnerValue1\":\"Special\"}]}]", output);
    }

    @Test
    public void dollarBackBracket() throws IOException { //Should act as "$"
        String output = buildJsonPath(sourceA, "$]", "VALUE");
        assertEquals("[{\"StringType\":\"String\",\"NumberType\":123,\"BooleanType\":true,\"NullType\":null,\"ObjectType\":{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},\"ArrayType\":[1,2,3,4,5,6,7],\"ObjectType2\":{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},\"ArrayType2\":[9,9,{\"InnerValue1\":\"Special\"}]}]", output);
    }

    @Test
    public void dollarDotBackBracket() throws IOException { //As there is no start bracket, reads as key "$."
        String output = buildJsonPath(sourceC,"$.]", "VALUE");
        assertEquals("[\"DollarDot\"]", output);
    }

    @Test
    public void dollarDollar() throws IOException { //$ gets the current object in most cases, so here it equals just $
        String output = buildJsonPath(sourceA,"$.$", "VALUE");
        assertEquals("[{\"StringType\":\"String\",\"NumberType\":123,\"BooleanType\":true,\"NullType\":null,\"ObjectType\":{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},\"ArrayType\":[1,2,3,4,5,6,7],\"ObjectType2\":{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},\"ArrayType2\":[9,9,{\"InnerValue1\":\"Special\"}]}]", output);
    }

    @Test
    public void dollarDotBackBracketEndless() throws IOException { //No matter how many back brackets there are, only the first one matters.
        String output = buildJsonPath(sourceC,"$.]]]]]]]]]]]]]]]]]]]]]]]]]", "VALUE");
        assertEquals("[\"DollarDot\"]", output);
    }

    @Test
    public void onlyKey() throws IOException {
        String output = buildJsonPath(sourceA,"StringType", "VALUE");
        assertEquals("[\"String\"]", output);
    }

    @Test
    public void onlyArrayIndex() throws IOException {
        String output = buildJsonPath(sourceD,"0", "VALUE");
        assertEquals("[5]", output);
    }

    @Test
    public void dollarGet() throws IOException {
        String output = buildJsonPath(sourceA,"$.NumberType", "VALUE");
        assertEquals("[123]", output);
    }

    @Test
    public void dollarGetBracket() throws IOException {
        String output = buildJsonPath(sourceA,"$[NumberType]", "VALUE");
        assertEquals("[123]", output);
    }

    @Test
    public void dollarGetBracketQuote() throws IOException {
        String output = buildJsonPath(sourceA,"$['NumberType']", "VALUE");
        assertEquals("[123]", output);
    }

    @Test
    public void dollarGetQuote() throws IOException {
        String output = buildJsonPath(sourceA,"$.'NumberType'", "VALUE");
        assertEquals("[123]", output);
    }

    @Test
    public void dollarGetGet() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.InnerValue1", "VALUE");
        assertEquals("[\"ONE\"]", output);
    }

    @Test
    public void dollarGetGetBracket() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType[InnerValue2]", "VALUE");
        assertEquals("[\"TWO\"]", output);
    }

    @Test
    public void dollarGetGetQuote() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.'InnerValue3'", "VALUE");
        assertEquals("[\"THREE\"]", output);
    }

    @Test
    public void dollarGetBracketGetBracket() throws IOException {
        String output = buildJsonPath(sourceA,"$[ObjectType][InnerValue1]", "VALUE");
        assertEquals("[\"ONE\"]", output);
    }

    @Test
    public void dollarGetBracketMiddleDot() throws IOException {
        String output = buildJsonPath(sourceA,"$[ObjectType2.InnerValue2]InnerValue3]", "VALUE"); //Treats it all as one word
        assertEquals("NO MATCHES", output);
    }


    @Test
    public void getArraySpecific() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.1", "VALUE");
        assertEquals("[2]", output);
    }

    @Test
    public void getArrayInvalidIndex() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.StringType", "VALUE");
        assertEquals("NO MATCHES", output);
    }


    @Test
    public void getArrayOutOfBounds() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.999", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayNegative() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.-2", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    //N = Number, X = No Input, Z = Negative

    @Test
    public void getArrayNN() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.1:3", "VALUE");
        assertEquals("[2,3]", output);
    }

    @Test
    public void getArrayNNN() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:4:2", "VALUE");
        assertEquals("[1,3]", output);
    }

    @Test
    public void getArrayNX() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.3:", "VALUE");
        assertEquals("[4,5,6,7]", output);
    }

    @Test
    public void getArrayNXX() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.2::", "VALUE");
        assertEquals("[3,4,5,6,7]", output);
    }

    @Test
    public void getArrayXX() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:", "VALUE");
        assertEquals("[1,2,3,4,5,6,7]", output);
    }

    @Test
    public void getArrayXXX() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.::", "VALUE");
        assertEquals("[1,2,3,4,5,6,7]", output);
    }

    @Test
    public void getArrayXN() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:4", "VALUE");
        assertEquals("[1,2,3,4]", output);
    }

    @Test
    public void getArrayXNX() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:5:", "VALUE");
        assertEquals("[1,2,3,4,5]", output);
    }

    @Test
    public void getArrayXXN() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.::3", "VALUE");
        assertEquals("[1,4,7]", output);
    }


    @Test
    public void getArrayXNN() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:5:2", "VALUE");
        assertEquals("[1,3,5]", output);
    }



    @Test
    public void getArrayNNX() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.::2", "VALUE");
        assertEquals("[1,3,5,7]", output);
    }

    @Test
    public void getArrayNXN() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.2::2", "VALUE");
        assertEquals("[3,5,7]", output);
    }

    @Test
    public void getArrayZX() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.-2:", "VALUE");
        assertEquals("[6,7]", output);
    }

    @Test
    public void getArrayXZ() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:-3", "VALUE");
        assertEquals("[1,2,3,4]", output);
    }

    @Test
    public void getArrayZZX() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.::-2", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayZZZ() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.-1:-2:-1", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayXZX() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:-1:", "VALUE");
        assertEquals("[1,2,3,4,5,6]", output);
    }

    @Test
    public void getArrayXXZ() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.::-2", "VALUE");
        assertEquals("NO MATCHES", output);
    }


    @Test
    public void getArrayOutOfBoundsFirst() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.999:0:0", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayOutOfBoundsSecond() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:999:0", "VALUE");
        assertEquals("[1,2,3,4,5,6,7]", output);
    }

    @Test
    public void getArrayOutOfBoundsThird() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:0:999", "VALUE");
        assertEquals("[1]", output);
    }

    @Test
    public void getArrayOutOfBoundsFirstNegative() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.-999:0:0", "VALUE");
        assertEquals("[1,2,3,4,5,6,7]", output);
    }



    @Test
    public void getArrayOutOfBoundsSecondNegative() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:-999:0", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayOutOfBoundsThirdNegative() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:0:-999", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayTooManyColons() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:6:2:", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getObjectColons() throws IOException {
        String output = buildJsonPath(sourceC,"$.2:3:4", "VALUE");
        assertEquals("[\"Two Three Four\"]", output);
    }

    @Test
    public void getArrayBrackets() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType[0:4:2]", "VALUE");
        assertEquals("[1,3]", output);
    }

    @Test
    public void getArrayBracketsQuote() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType['0:4:2']", "VALUE");
        assertEquals("[1,3]", output);
    }

    @Test
    public void getArrayQuote() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.'0:4:2'", "VALUE");
        assertEquals("[1,3]", output);
    }

    @Test
    public void onlyArrayRange() throws IOException {
        String output = buildJsonPath(sourceD,"0:0:2", "VALUE");
        assertEquals("[5,3,{\"Object\":\"Word2\",\"Object2\":\"Word3\"},[{\"ITEM1\":{\"ITEM2\":\"FOUND IT\"}},{\"ITEM2\":[5,6,8]}]]", output);
    }

    @Test
    public void onlyDot() throws IOException {
        String output = buildJsonPath(sourceA,".", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void nameWithDot() throws IOException {
        String output = buildJsonPath(sourceC,"$.nameWith.Dot", "VALUE"); //Though nameWith.Dot exists, instructions treat it as two different words.
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void nameWithDotQuote() throws IOException {
        String output = buildJsonPath(sourceC,"$.'nameWith.Dot'", "VALUE"); //Quotations don't affect how it is read.
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void nameWithDotBracket() throws IOException {
        String output = buildJsonPath(sourceC,"$[nameWith.Dot]", "VALUE"); //Dots within brackets are treated as part of the word
        assertEquals("[\"DOT\"]", output);
    }

    @Test
    public void nameWithDotBracketQuote() throws IOException {
        String output = buildJsonPath(sourceC,"$['nameWith.Dot']", "VALUE"); //Dots within brackets are treated as part of the word
        assertEquals("[\"DOT\"]", output);
    }

    @Test
    public void onlyDoubleDot() throws IOException {
        String output = buildJsonPath(sourceA,"..", "VALUE"); //Reads as $[]..
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarAll() throws IOException {
        String output = buildJsonPath(sourceA,"$..", "VALUE"); //Reads as $[]..
        assertEquals("[{\"StringType\":\"String\",\"NumberType\":123,\"BooleanType\":true,\"NullType\":null,\"ObjectType\":{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},\"ArrayType\":[1,2,3,4,5,6,7],\"ObjectType2\":{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},\"ArrayType2\":[9,9,{\"InnerValue1\":\"Special\"}]},null,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}],{\"InnerValue3\":\"THREEFOUR\"},{\"InnerValue1\":\"Special\"}]", output);
    }

    @Test
    public void dollarAllBrackets() throws IOException {
        String output = buildJsonPath(sourceA,"$[[", "VALUE"); //Reads as $[]..
        assertEquals("[{\"StringType\":\"String\",\"NumberType\":123,\"BooleanType\":true,\"NullType\":null,\"ObjectType\":{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},\"ArrayType\":[1,2,3,4,5,6,7],\"ObjectType2\":{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},\"ArrayType2\":[9,9,{\"InnerValue1\":\"Special\"}]},null,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}],{\"InnerValue3\":\"THREEFOUR\"},{\"InnerValue1\":\"Special\"}]", output);
    }

    @Test
    public void dollarAllBracketsQuotes() throws IOException {
        String output = buildJsonPath(sourceA,"$['['", "VALUE"); //Reads as $[]..
        assertEquals("[{\"StringType\":\"String\",\"NumberType\":123,\"BooleanType\":true,\"NullType\":null,\"ObjectType\":{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},\"ArrayType\":[1,2,3,4,5,6,7],\"ObjectType2\":{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},\"ArrayType2\":[9,9,{\"InnerValue1\":\"Special\"}]},null,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}],{\"InnerValue3\":\"THREEFOUR\"},{\"InnerValue1\":\"Special\"}]", output);
    }

    @Test
    public void dollarAllBracketsClosed() throws IOException {
        String output = buildJsonPath(sourceC, "$[[]", "VALUE"); //Reads as $.[]
        assertEquals("[\"Blank\"]", output);
    }

    @Test
    public void dollarAllGet() throws IOException {
        String output = buildJsonPath(sourceA, "$..InnerValue2", "VALUE"); //Reads as $.[]
        assertEquals("[\"TWO\",{\"InnerValue3\":\"THREEFOUR\"}]", output);
    }

    @Test
    public void dollarAllGetBrackets() throws IOException {
        String output = buildJsonPath(sourceA, "$[[InnerValue2]", "VALUE"); //Reads as $.[]
        assertEquals("[\"TWO\",{\"InnerValue3\":\"THREEFOUR\"}]", output);
    }

    @Test
    public void dollarAllGetBracketsQuote() throws IOException {
        String output = buildJsonPath(sourceA, "$['['InnerValue2']", "VALUE"); //Reads as $.[]
        assertEquals("[\"TWO\",{\"InnerValue3\":\"THREEFOUR\"}]", output);
    }

    @Test
    public void dollarAllGetBracketDot() throws IOException {
        String output = buildJsonPath(sourceA, "$[.InnerValue2", "VALUE"); //Reads as $.[]
        assertEquals("[\"TWO\",{\"InnerValue3\":\"THREEFOUR\"}]", output);
    }

    @Test
    public void dollarAllGetBracketDotClosed() throws IOException { //Treated as single word
        String output = buildJsonPath(sourceA, "$[.InnerValue2]", "VALUE"); //Reads as $.[]
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarAllGetGet() throws IOException {
        String output = buildJsonPath(sourceA, "$..InnerValue2.InnerValue3", "VALUE"); //Reads as $.[]
        assertEquals("[\"THREEFOUR\"]", output);
    }

    @Test
    public void dollarAllGetGetBrackets() throws IOException {
        String output = buildJsonPath(sourceA, "$[[InnerValue2][InnerValue3]", "VALUE"); //Reads as $.[]
        assertEquals("[\"THREEFOUR\"]", output);
    }

    @Test
    public void dollarAllGetGetBracketsMiddleDot() throws IOException { //Interprets it as whole word
        String output = buildJsonPath(sourceA, "$[[InnerValue2.InnerValue3]", "VALUE"); //Reads as $.[]
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarGetStringIndex() throws IOException {
        String output = buildJsonPath(sourceA,"$.StringType.4", "VALUE"); //Script needs brackets around it, or quotes.
        assertEquals("[\"n\"]", output);
    }


    @Test
    public void onlyWild() throws IOException {
        String output = buildJsonPath(sourceA,"*", "VALUE");
        assertEquals("[\"String\",123,true,null,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}]]", output);
    }

    @Test
    public void dollarWild() throws IOException {
        String output = buildJsonPath(sourceA,"$.*", "VALUE");
        assertEquals("[\"String\",123,true,null,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}]]", output);
    }

    @Test
    public void dollarWildBracket() throws IOException {
        String output = buildJsonPath(sourceA,"$[*", "VALUE");
        assertEquals("[\"String\",123,true,null,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}]]", output);
    }

    @Test
    public void dollarWildBracketPair() throws IOException {
        String output = buildJsonPath(sourceA,"$[*]", "VALUE");
        assertEquals("[\"String\",123,true,null,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}]]", output);
    }

    @Test
    public void dollarWildBracketQuotePair() throws IOException {
        String output = buildJsonPath(sourceA,"$['*']", "VALUE");
        assertEquals("[\"String\",123,true,null,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}]]", output);
    }

    @Test
    public void dollarWildQuote() throws IOException {
        String output = buildJsonPath(sourceA,"$.'*'", "VALUE");
        assertEquals("[\"String\",123,true,null,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}]]", output);
    }

    @Test
    public void dollarWildGetUpper() throws IOException {
        String output = buildJsonPath(sourceA,"$.*.NumberValue", "VALUE"); //Since NumberValue was on the original top layer, it no longer exists.
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarWildGetInner() throws IOException {
        String output = buildJsonPath(sourceA,"$.*.InnerValue4", "VALUE"); //Since innerValue4 was in an object, it can be found.
        assertEquals("[\"FIVESIX\"]", output);
    }

    @Test
    public void dollarAllWild() throws IOException {
        String output = buildJsonPath(sourceA,"$..*", "VALUE"); //Since innerValue4 was in an object, it can be found.
        assertEquals("[\"String\",123,true,null,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}],\"ONE\",\"TWO\",\"THREE\",1,2,3,4,5,6,7,\"ONETWO\",{\"InnerValue3\":\"THREEFOUR\"},\"FIVESIX\",9,9,{\"InnerValue1\":\"Special\"},\"THREEFOUR\",\"Special\"]", output);
    }

    @Test
    public void dollarWildAll() throws IOException {
        String output = buildJsonPath(sourceA,"$.*..", "VALUE"); //Since innerValue4 was in an object, it can be found.
        assertEquals("[\"String\",123,true,null,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}],{\"InnerValue3\":\"THREEFOUR\"},{\"InnerValue1\":\"Special\"}]", output);
    }

    @Test
    public void onlyScript() throws IOException {
        String output = buildJsonPath(sourceC,"(@.length-1)", "VALUE"); //Script needs brackets around it, or quotes.
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void onlyScriptQuotes() throws IOException {
        String output = buildJsonPath(sourceC,"'(@.length-1)'", "VALUE"); //Reads as $[].Script. As script is String, returns last character.
        assertEquals("[\"k\"]", output);
    }

    @Test
    public void onlyScriptBrackets() throws IOException {
        String output = buildJsonPath(sourceC,"[(@.length-1)]", "VALUE"); //Script needs brackets around it, or quotes.
        assertEquals("[\"k\"]", output);
    }

    @Test
    public void onlyScriptQuoteAndBracket() throws IOException {
        String output = buildJsonPath(sourceC,"[(@.length-1)'", "VALUE"); //Script needs brackets around it, or quotes.
        assertEquals("[\"k\"]", output);
    }

    @Test
    public void dollarGetScriptDot() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.(@.length-1)", "VALUE"); //Script needs brackets around it, or quotes.
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarGetScriptBracket() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType[(@.length-1)]", "VALUE"); //Script needs brackets around it, or quotes.
        assertEquals("[7]", output);
    }

    @Test
    public void dollarGetScriptQuotes() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(@.length-1)'", "VALUE"); //Script needs brackets around it, or quotes.
        assertEquals("[7]", output);
    }

    @Test
    public void dollarAllScript() throws IOException {
        String output = buildJsonPath(sourceA,"$.'(@.length-1)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarGetAllScript() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(@.length-1)'", "VALUE");
        assertEquals("[7]", output);
    }

    @Test
    public void dollarWildScript() throws IOException {
        String output = buildJsonPath(sourceA,"$.*'(@.length-1)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarWildAllScript() throws IOException {
        String output = buildJsonPath(sourceA,"$.*.'(@.length-1)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarGetScriptFollowUp() throws IOException { //Ignores follow-up without dot
        String output = buildJsonPath(sourceA,"$.ArrayType2'(@.length-1)'InnerValue1", "VALUE");
        assertEquals("[{\"InnerValue1\":\"Special\"}]", output);
    }

    @Test
    public void dollarGetScriptFollowUpDot() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType2'(@.length-1)'.InnerValue1", "VALUE");
        assertEquals("[\"Special\"]", output);
    }

    @Test
    public void dollarGetScriptFollowUpBrackets() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType2'(@.length-1)'[InnerValue1]", "VALUE");
        assertEquals("[\"Special\"]", output);
    }

    @Test
    public void dollarGetScriptFollowUpWild() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType2'(@.length-1)'.*", "VALUE");
        assertEquals("[\"Special\"]", output);
    }

    @Test
    public void dollarAllScriptFollowUpWild() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType2'(@.length-1)'.*", "VALUE");
        assertEquals("[\"Special\"]", output);
    }

    @Test
    public void allScriptLength() throws IOException {
        String output = buildJsonPath(sourceA,"$..'(@.length-1)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayAllScriptLength() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType..'(@.length-1)'", "VALUE");
        assertEquals("[7]", output);
    }

    @Test
    public void scriptBadSpacing() throws IOException { //Script ignores most bad spacing
        String output = buildJsonPath(sourceA,"$.ArrayType'(      @      .length      -      3      )'", "VALUE");
        assertEquals("[5]", output);
    }

    @Test
    public void scriptBadSpacingStartComma() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'   (@.length-4)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void scriptBadSpacingBackComma() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(@.length-4)   '", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void scriptInvalid() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(@.invalid-3)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getNoAtSymbol() throws IOException { //In example, defaults length to 5. Ours does not do this.
        String output = buildJsonPath(sourceA,"$.ArrayType'(length-3)'", "VALUE");
        assertEquals("[5]", output);
    }

    @Test
    public void scriptLengthMultipleEquations() throws IOException { //Example allowed multiple equations, ours does not (What reason is there to have multiple?)
        String output = buildJsonPath(sourceA,"$.ArrayType'(length-3+2)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void scriptLengthExceeded() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(length+99999)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void scriptLengthBelowZero() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(length-99999)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void scriptMultiply() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(length*0.25)'", "VALUE");
        assertEquals("[2]", output);
    }

    @Test
    public void scriptDivide() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(length/2)'", "VALUE");
        assertEquals("[4]", output);
    }










    @Test
    public void onlyFilter() throws IOException {
        String output = buildJsonPath(sourceA,"?(@ )", "VALUE"); //Acts like wild, but does not retrieve values with JsonNull
        assertEquals("[\"String\",123,true,{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},[1,2,3,4,5,6,7],{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},[9,9,{\"InnerValue1\":\"Special\"}]]", output);
    }

    @Test
    public void onlyFilterKeyword() throws IOException {
        String output = buildJsonPath(sourceD,"$.6'?(@.ITEM1)'", "VALUE"); //Cannot read values.
        assertEquals("[{\"ITEM1\":{\"ITEM2\":\"FOUND IT\"}}]", output);
    }

    @Test
    public void onlyFilterKeywordDot() throws IOException {
        String output = buildJsonPath(sourceD,"$.6.'?(@.ITEM1)'", "VALUE"); //Cannot read values.
        assertEquals("[{\"ITEM1\":{\"ITEM2\":\"FOUND IT\"}}]", output);
    }

    @Test
    public void filterNegate() throws IOException { //INOPERABLE RIGHT NOW FOR SOURCE D
        String output = buildJsonPath(sourceD,"$'?(!@.Object2)'", "VALUE");
        assertEquals("[{\"Object\":\"Word\"}]", output);
    }

    @Test
    public void filterEquals() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year == 2022)'","VALUE");
        assertEquals("[{\"title\":\"How to Science\",\"year\":2022,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":true}]", output);
    }

    @Test
    public void filterNotEquals() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.available != true)'","VALUE");
        assertEquals("[{\"title\":\"Fifty Shades of Grey\",\"year\":2012,\"genre\":\"fantasy\",\"age rating\":\"adult\",\"available\":false},{\"title\":\"The bad man's book\",\"year\":1925,\"genre\":\"history\",\"age rating\":\"adult\",\"available\":false},{\"title\":\"Harry Potter and the Cupboard that didn't exist\",\"year\":3000,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":false}]", output);
    }

    @Test
    public void filterLessThan() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year > 2020)'","VALUE");
        assertEquals("[{\"title\":\"How to Science\",\"year\":2022,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":true},{\"title\":\"Harry Potter and the Cupboard that didn't exist\",\"year\":3000,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":false}]", output);
    }

    @Test
    public void filterLessThanEquals() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year >= 2020)'","VALUE");
        assertEquals("[{\"title\":\"How to Science\",\"year\":2022,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":true},{\"title\":\"Harry Potter and the Cupboard that didn't exist\",\"year\":3000,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":false},{\"title\":\"My Loving Family\",\"year\":2020,\"genre\":\"non-fiction\",\"age rating\":\"all\",\"available\":true}]", output);
    }

    @Test
    public void filterGreaterThan() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year < 1987)'","VALUE");
        assertEquals("[{\"title\":\"The bad man's book\",\"year\":1925,\"genre\":\"history\",\"age rating\":\"adult\",\"available\":false},{\"title\":\"Mystery Book\",\"year\":0,\"genre\":\"unknown\",\"age rating\":\"all\",\"available\":true}]", output);
    }

    @Test
    public void filterGreaterThanEquals() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year <= 1987)'","VALUE");
        assertEquals("[{\"title\":\"Lord of the Onion Rings\",\"year\":1987,\"genre\":\"fantasy\",\"age rating\":\"adult\",\"available\":true},{\"title\":\"The bad man's book\",\"year\":1925,\"genre\":\"history\",\"age rating\":\"adult\",\"available\":false},{\"title\":\"Mystery Book\",\"year\":0,\"genre\":\"unknown\",\"age rating\":\"all\",\"available\":true}]", output);
    }

    @Test
    public void filterRegex() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.title =~ .*the.*)'","VALUE");
        assertEquals("[{\"title\":\"Lord of the Onion Rings\",\"year\":1987,\"genre\":\"fantasy\",\"age rating\":\"adult\",\"available\":true},{\"title\":\"Harry Potter and the Cupboard that didn't exist\",\"year\":3000,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":false}]", output);
    }

    @Test
    public void filterIn() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.genre in ['food', 'history'])'","VALUE");
        assertEquals("[{\"title\":\"The bad man's book\",\"year\":1925,\"genre\":\"history\",\"age rating\":\"adult\",\"available\":false},{\"title\":\"Cooking with Kids\",\"year\":1988,\"genre\":\"food\",\"age rating\":\"all\",\"available\":true}]", output);
    }

    @Test
    public void filterNotIn() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.genre nin ['food', 'history'])'","VALUE");
        assertEquals("[{\"title\":\"Lord of the Onion Rings\",\"year\":1987,\"genre\":\"fantasy\",\"age rating\":\"adult\",\"available\":true},{\"title\":\"How to Science\",\"year\":2022,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":true},{\"title\":\"Fifty Shades of Grey\",\"year\":2012,\"genre\":\"fantasy\",\"age rating\":\"adult\",\"available\":false},{\"title\":\"Harry Potter and the Cupboard that didn't exist\",\"year\":3000,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":false},{\"title\":\"My Loving Family\",\"year\":2020,\"genre\":\"non-fiction\",\"age rating\":\"all\",\"available\":true},{\"title\":\"Mystery Book\",\"year\":0,\"genre\":\"unknown\",\"age rating\":\"all\",\"available\":true}]", output);
    }

    @Test
    public void filterSizeString() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.title size 14)'","VALUE");
        assertEquals("[{\"title\":\"How to Science\",\"year\":2022,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":true}]", output);
    }

    @Test
    public void filterSizeArray() throws IOException {
        String output = buildJsonPath(sourceD,"$.'?(@ size 3)'", "VALUE");
        assertEquals("[[62,31,16],[5,6,8]]", output);
    }

    @Test
    public void filterContainsString() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.title contains the)'", "VALUE");
        assertEquals("[{\"title\":\"Lord of the Onion Rings\",\"year\":1987,\"genre\":\"fantasy\",\"age rating\":\"adult\",\"available\":true},{\"title\":\"Harry Potter and the Cupboard that didn't exist\",\"year\":3000,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":false}]", output);
    }

    @Test
    public void filterContainsArray() throws IOException {
        String output = buildJsonPath(sourceA,"$'?(@ contains 7)'", "VALUE");
        assertEquals("[[1,2,3,4,5,6,7]]", output);
    }

    @Test
    public void filterEmptyTrue() throws IOException {
        String output = buildJsonPath(sourceC,"$'?(@ empty true)'", "VALUE");
        assertEquals("[{},[],null]", output);
    }

    @Test
    public void filterEmptyFalse() throws IOException {
        String output = buildJsonPath(sourceC,"$'?(@ empty false)'", "VALUE");
        assertEquals("[\"DOT\",\"BSlash\",\"Should not appear\",\"Blank\",\"Bad Bracket Left\",\"Bad Bracket Right\",\"Zero\",\"DollarDot\",\"Two Three Four\",\"One One One One\",\"Outside\",{\"keyword\":\"Inside1\"},{\"innerArray\":[1,3,{\"keyword\":\"Inside2\"}]}]", output);
    }

    @Test
    public void filterEmptyInvalid() throws IOException {
        String output = buildJsonPath(sourceC,"$'?(!@ empty yes)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterAND() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.year > 2000 && @.genre != 'non-fiction')", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterOR() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.year < 1950 || @.genre == 'non-fiction')", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterANDplusOR() throws IOException { //OR Stronger than AND
        String output = buildJsonPath(sourceB,"$.library.?(@.available == false && @.genre == 'fantasy' || @.year == 0)","VALUE");
        assertEquals("NO MATCHES", output);
    }


    @Test
    public void filterGetInner() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.available == false).year", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterGetUpper() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.available == false).2", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterGetInvalid() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.INVALID == null)", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterAll() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@ )..age rating", "VALUE");
        assertEquals("[\"adult\",\"child\",\"adult\",\"adult\",\"all\",\"child\",\"all\",\"all\"]", output);
    }

    @Test
    public void filterWild() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.title).*", "VALUE");
        assertEquals("NO MATCHES", output);
    }






    @Test
    public void onlyComma() throws IOException {
        String output = buildJsonPath(sourceA,",", "VALUE"); //Reads as $[],
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterCommaBlank() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year == 3000)',", "VALUE");
        assertEquals("[{\"title\":\"Harry Potter and the Cupboard that didn't exist\",\"year\":3000,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":false}]", output);
    }

    @Test
    public void filterCommaKey() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year == 3000)',genre", "VALUE");
        assertEquals("[{\"title\":\"Harry Potter and the Cupboard that didn't exist\",\"year\":3000,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":false}]", output);
    }

    @Test
    public void filterCommaDotKey() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year == 3000)',.genre", "VALUE");
        assertEquals("[\"fantasy\"]", output);
    }

    @Test
    public void filterCommaAll() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year == 3000)',..age rating", "VALUE");
        assertEquals("[\"child\"]", output);
    }

    @Test
    public void filterCommaWild() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year == 3000)',.*", "VALUE");
        assertEquals("[\"Harry Potter and the Cupboard that didn't exist\",3000,\"fantasy\",\"child\",false]", output);
    }

    @Test
    public void filterScript() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'?(@ )''(@.length-1)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterFilter() throws IOException { //SOURCE D IS INOPERABLE RIGHT NOW
        String output = buildJsonPath(sourceD,"$.'?(@.ITEM1)''?(@.ITEM2)'", "VALUE");
        assertEquals("[{\"ITEM2\":\"FOUND IT\"}]", output);
    }

    @Test
    public void allFilter() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.'?(@.available != false)'", "VALUE");
        assertEquals("[{\"title\":\"Lord of the Onion Rings\",\"year\":1987,\"genre\":\"fantasy\",\"age rating\":\"adult\",\"available\":true},{\"title\":\"How to Science\",\"year\":2022,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":true},{\"title\":\"Cooking with Kids\",\"year\":1988,\"genre\":\"food\",\"age rating\":\"all\",\"available\":true},{\"title\":\"My Loving Family\",\"year\":2020,\"genre\":\"non-fiction\",\"age rating\":\"all\",\"available\":true},{\"title\":\"Mystery Book\",\"year\":0,\"genre\":\"unknown\",\"age rating\":\"all\",\"available\":true},\"Lord of the Onion Rings\",1987,\"fantasy\",\"adult\",true,\"How to Science\",2022,\"fantasy\",\"child\",\"Fifty Shades of Grey\",2012,\"fantasy\",\"adult\",false,\"The bad man's book\",1925,\"history\",\"adult\",\"Cooking with Kids\",1988,\"food\",\"all\",\"Harry Potter and the Cupboard that didn't exist\",3000,\"fantasy\",\"child\",\"My Loving Family\",2020,\"non-fiction\",\"all\",\"Mystery Book\",0,\"unknown\",\"all\"]", output);
    }

    @Test
    public void allFilterTriple() throws IOException {
        String output = buildJsonPath(sourceB,"$.library..'?(@.available != false)'", "VALUE");
        assertEquals("[{\"title\":\"Lord of the Onion Rings\",\"year\":1987,\"genre\":\"fantasy\",\"age rating\":\"adult\",\"available\":true},{\"title\":\"How to Science\",\"year\":2022,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":true},{\"title\":\"Cooking with Kids\",\"year\":1988,\"genre\":\"food\",\"age rating\":\"all\",\"available\":true},{\"title\":\"My Loving Family\",\"year\":2020,\"genre\":\"non-fiction\",\"age rating\":\"all\",\"available\":true},{\"title\":\"Mystery Book\",\"year\":0,\"genre\":\"unknown\",\"age rating\":\"all\",\"available\":true},\"Lord of the Onion Rings\",1987,\"fantasy\",\"adult\",true,\"How to Science\",2022,\"fantasy\",\"child\",\"Fifty Shades of Grey\",2012,\"fantasy\",\"adult\",false,\"The bad man's book\",1925,\"history\",\"adult\",\"Cooking with Kids\",1988,\"food\",\"all\",\"Harry Potter and the Cupboard that didn't exist\",3000,\"fantasy\",\"child\",\"My Loving Family\",2020,\"non-fiction\",\"all\",\"Mystery Book\",0,\"unknown\",\"all\"]", output);
    }

    @Test
    public void filterWithNull() throws IOException {
        String output = buildJsonPath(sourceA,"$'?(@.InnerValue1)'.InnerValue1", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterTooManyItems() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.available != true false)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }
    @Test
    public void filterNoInstruction() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?()'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterNoValue() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.title ==)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterTooManySpaces() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(        @.year         >         2900         )'", "VALUE");
        assertEquals("[{\"title\":\"Harry Potter and the Cupboard that didn't exist\",\"year\":3000,\"genre\":\"fantasy\",\"age rating\":\"child\",\"available\":false}]", output);
    }

    @Test
    public void filterNothingFollowOR() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.title ||)'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterNothingFollowAND() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year > 2000 && )'", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getCommaBlank() throws IOException {
        String output = buildJsonPath(sourceA,"$.StringType,", "VALUE");
        assertEquals("[\"String\"]", output);
    }
    @Test
    public void getCommaSingle() throws IOException {
        String output = buildJsonPath(sourceA,"$.StringType,NumberType", "VALUE");
        assertEquals("[\"String\",123]", output);
    }

    @Test
    public void getCommaMulti() throws IOException {
        String output = buildJsonPath(sourceA,"$.NumberType,NullType,BooleanType", "VALUE");
        assertEquals("[123,null,true]", output);
    }

    @Test
    public void getCommaDuplicate() throws IOException {
        String output = buildJsonPath(sourceA,"$.NullType,NullType", "VALUE");
        assertEquals("[null,null]", output);
    }

    @Test
    public void getInnerValueCommaGet() throws IOException {
        String output = buildJsonPath(sourceA,"$.BooleanType,ArrayType.2", "VALUE");
        assertEquals("[true,3]", output);
    }

    @Test
    public void getCommaGetInnerValue() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.InnerValue1,InnerValue3", "VALUE");
        assertEquals("[\"ONE\",\"THREE\"]", output);
    }

    @Test
    public void getCommaGetUpperValue() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.InnerValue3,NumberType", "VALUE");
        assertEquals("[\"THREE\"]", output);
    }

    @Test
    public void getCommaDot() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType,.", "VALUE");
        assertEquals("[{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"}]", output);
    }

    @Test
    public void getCommaDotGet() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType,.NumberType", "VALUE");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getCommaDotGetValid() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType,.InnerValue2", "VALUE");
        assertEquals("[\"TWO\"]", output);
    }

    @Test
    public void getCommaDollar() throws IOException {
        String output = buildJsonPath(sourceA,"$.NumberType,$", "VALUE");
        assertEquals("[123,{\"StringType\":\"String\",\"NumberType\":123,\"BooleanType\":true,\"NullType\":null,\"ObjectType\":{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"},\"ArrayType\":[1,2,3,4,5,6,7],\"ObjectType2\":{\"InnerValue1\":\"ONETWO\",\"InnerValue2\":{\"InnerValue3\":\"THREEFOUR\"},\"InnerValue4\":\"FIVESIX\"},\"ArrayType2\":[9,9,{\"InnerValue1\":\"Special\"}]}]", output);
    }

    @Test
    public void getCommaDollarInnerKey() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.InnerValue2,$", "VALUE");
        assertEquals("[\"TWO\",{\"InnerValue1\":\"ONE\",\"InnerValue2\":\"TWO\",\"InnerValue3\":\"THREE\"}]", output);
    }

    @Test
    public void getCommaDollerUpperKey() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.InnerValue2,$.NumberType", "VALUE");
        assertEquals("NO MATCHES", output);
    }



    @Test
    public void dollarMissingBracket() throws IOException {
        String output = buildJsonPath(sourceA,"[", "VALUE");
        assertEquals("NO MATCHES", output);
    }


    @Test
    public void dollarMissingGetBracket() throws IOException {
        String output = buildJsonPath(sourceA,"[NullType", "VALUE");
        assertEquals("NO MATCHES", output);
    }





    //
    //
    //

    @Test
    public void noInputPATH() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceC,"", "PATH");
        assertEquals("[\"$['']\"]", output);
    }

    @Test
    public void dollarOnlyPATH() throws IOException {
        String output = buildJsonPath(sourceA, "$", "PATH");
        assertEquals("[\"$\"]", output);
    }

    @Test
    public void dollarDotPATH() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceA,"$.", "PATH");
        assertEquals("[\"$\"]", output);
    }

    @Test
    public void dollarDotQuotePATH() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceA,"$.'", "PATH");
        assertEquals("[\"$\"]", output);
    }

    @Test
    public void dollarDotQuoteDotQuotePATH() throws IOException { //Should be read as ALL
        String output = buildJsonPath(sourceC,"$.'.'", "PATH");
        assertEquals("[\"$\",\"$['empty object']\",\"$['empty array']\",\"$['empty word']\",\"$['filledObject']\",\"$['filledobject']\",\"$['filledobject']['innerArray']\",\"$['filledobject']['innerArray'][2]\"]", output);
    }

    @Test
    public void dollarDotDoubleQuotePATH() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceC,"$.''", "PATH");
        assertEquals("[\"$['']\"]", output);
    }

    @Test
    public void dollarDotDoubleQuoteDotPATH() throws IOException { //Should be read as ALL
        String output = buildJsonPath(sourceC,"$.''.", "PATH");
        assertEquals("[\"$\",\"$['empty object']\",\"$['empty array']\",\"$['empty word']\",\"$['filledObject']\",\"$['filledobject']\",\"$['filledobject']['innerArray']\",\"$['filledobject']['innerArray'][2]\"]", output);
    }

    @Test
    public void dollarSingleQuoteInBracketsPATH() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceC,"$[']", "PATH");
        assertEquals("[\"$['']\"]", output);
    }

    @Test
    public void dollarDoubleQuoteInBracketsPATH() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceC,"$['']", "PATH");
        assertEquals("[\"$['']\"]", output);
    }

    @Test
    public void dollarSemiColonEndBracketPATH() throws IOException { //Semicolon acts like start bracket, so should return key "" instead of key "$;"
        String output = buildJsonPath(sourceC,"$;]", "PATH");
        assertEquals("[\"$['']\"]", output);
    }


    @Test
    public void dollarBracketPATH() throws IOException { //Defaults to searching for key ""
        String output = buildJsonPath(sourceA,"$[", "PATH");
        assertEquals("[\"$\"]", output);
    }

    @Test
    public void dollarBackBracketPATH() throws IOException { //Should act as "$"
        String output = buildJsonPath(sourceA, "$]", "PATH");
        assertEquals("[\"$\"]", output);
    }

    @Test
    public void dollarDotBackBracketPATH() throws IOException { //As there is no start bracket, reads as key "$."
        String output = buildJsonPath(sourceC,"$.]", "PATH");
        assertEquals("[\"$['$.']\"]", output);
    }

    @Test
    public void dollarDollarPATH() throws IOException { //$ gets the current object in most cases, so here it equals just $
        String output = buildJsonPath(sourceA,"$.$", "PATH");
        assertEquals("[\"$\"]", output);
    }

    @Test
    public void dollarDotBackBracketEndlessPATH() throws IOException { //No matter how many back brackets there are, only the first one matters.
        String output = buildJsonPath(sourceC,"$.]]]]]]]]]]]]]]]]]]]]]]]]]", "PATH");
        assertEquals("[\"$['$.']\"]", output);
    }

    @Test
    public void onlyKeyPATH() throws IOException {
        String output = buildJsonPath(sourceA,"StringType", "PATH");
        assertEquals("[\"$['StringType']\"]", output);
    }

    @Test
    public void onlyArrayIndexPATH() throws IOException {
        String output = buildJsonPath(sourceD,"0", "PATH");
        assertEquals("[\"$[0]\"]", output);
    }

    @Test
    public void dollarGetPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.NumberType", "PATH");
        assertEquals("[\"$['NumberType']\"]", output);
    }

    @Test
    public void dollarGetBracketPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$[NumberType]", "PATH");
        assertEquals("[\"$['NumberType']\"]", output);
    }

    @Test
    public void dollarGetBracketQuotePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$['NumberType']", "PATH");
        assertEquals("[\"$['NumberType']\"]", output);
    }

    @Test
    public void dollarGetQuotePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.'NumberType'", "PATH");
        assertEquals("[\"$['NumberType']\"]", output);
    }

    @Test
    public void dollarGetGetPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.InnerValue1", "PATH");
        assertEquals("[\"$['ObjectType']['InnerValue1']\"]", output);
    }

    @Test
    public void dollarGetGetBracketPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType[InnerValue2]", "PATH");
        assertEquals("[\"$['ObjectType']['InnerValue2']\"]", output);
    }

    @Test
    public void dollarGetGetQuotePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.'InnerValue3'", "PATH");
        assertEquals("[\"$['ObjectType']['InnerValue3']\"]", output);
    }

    @Test
    public void dollarGetBracketGetBracketPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$[ObjectType][InnerValue1]", "PATH");
        assertEquals("[\"$['ObjectType']['InnerValue1']\"]", output);
    }

    @Test
    public void dollarGetBracketMiddleDotPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$[ObjectType2.InnerValue2]InnerValue3]", "PATH"); //Treats it all as one word
        assertEquals("NO MATCHES", output);
    }


    @Test
    public void getArraySpecificPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.1", "PATH");
        assertEquals("[\"$['ArrayType'][1]\"]", output);
    }

    @Test
    public void getArrayInvalidIndexPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.StringType", "PATH");
        assertEquals("NO MATCHES", output);
    }


    @Test
    public void getArrayOutOfBoundsPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.999", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayNegativePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.-2", "PATH");
        assertEquals("NO MATCHES", output);
    }

//N = Number, X = No Input, Z = Negative

    @Test
    public void getArrayNNPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.1:3", "PATH");
        assertEquals("[\"$['ArrayType'][1]\",\"$['ArrayType'][2]\"]", output);
    }

    @Test
    public void getArrayNNNPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:4:2", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][2]\"]", output);
    }

    @Test
    public void getArrayNXPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.3:", "PATH");
        assertEquals("[\"$['ArrayType'][3]\",\"$['ArrayType'][4]\",\"$['ArrayType'][5]\",\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void getArrayNXXPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.2::", "PATH");
        assertEquals("[\"$['ArrayType'][2]\",\"$['ArrayType'][3]\",\"$['ArrayType'][4]\",\"$['ArrayType'][5]\",\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void getArrayXXPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][1]\",\"$['ArrayType'][2]\",\"$['ArrayType'][3]\",\"$['ArrayType'][4]\",\"$['ArrayType'][5]\",\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void getArrayXXXPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.::", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][1]\",\"$['ArrayType'][2]\",\"$['ArrayType'][3]\",\"$['ArrayType'][4]\",\"$['ArrayType'][5]\",\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void getArrayXNPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:4", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][1]\",\"$['ArrayType'][2]\",\"$['ArrayType'][3]\"]", output);
    }

    @Test
    public void getArrayXNXPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:5:", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][1]\",\"$['ArrayType'][2]\",\"$['ArrayType'][3]\",\"$['ArrayType'][4]\"]", output);
    }

    @Test
    public void getArrayXXNPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.::3", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][3]\",\"$['ArrayType'][6]\"]", output);
    }


    @Test
    public void getArrayXNNPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:5:2", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][2]\",\"$['ArrayType'][4]\"]", output);
    }



    @Test
    public void getArrayNNXPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.::2", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][2]\",\"$['ArrayType'][4]\",\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void getArrayNXNPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.2::2", "PATH");
        assertEquals("[\"$['ArrayType'][2]\",\"$['ArrayType'][4]\",\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void getArrayZXPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.-2:", "PATH");
        assertEquals("[\"$['ArrayType'][5]\",\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void getArrayXZPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:-3", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][1]\",\"$['ArrayType'][2]\",\"$['ArrayType'][3]\"]", output);
    }

    @Test
    public void getArrayZZXPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.::-2", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayZZZPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.-1:-2:-1", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayXZXPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.:-1:", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][1]\",\"$['ArrayType'][2]\",\"$['ArrayType'][3]\",\"$['ArrayType'][4]\",\"$['ArrayType'][5]\"]", output);
    }

    @Test
    public void getArrayXXZPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.::-2", "PATH");
        assertEquals("NO MATCHES", output);
    }


    @Test
    public void getArrayOutOfBoundsFirstPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.999:0:0", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayOutOfBoundsSecondPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:999:0", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][1]\",\"$['ArrayType'][2]\",\"$['ArrayType'][3]\",\"$['ArrayType'][4]\",\"$['ArrayType'][5]\",\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void getArrayOutOfBoundsThirdPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:0:999", "PATH");
        assertEquals("[\"$['ArrayType'][0]\"]", output);
    }

    @Test
    public void getArrayOutOfBoundsFirstNegativePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.-999:0:0", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][1]\",\"$['ArrayType'][2]\",\"$['ArrayType'][3]\",\"$['ArrayType'][4]\",\"$['ArrayType'][5]\",\"$['ArrayType'][6]\"]", output);
    }


    @Test
    public void getArrayOutOfBoundsSecondNegativePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:-999:0", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayOutOfBoundsThirdNegativePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:0:-999", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayTooManyColonsPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.0:6:2:", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getObjectColonsPATH() throws IOException {
        String output = buildJsonPath(sourceC,"$.2:3:4", "PATH");
        assertEquals("[\"$['2:3:4']\"]", output);
    }

    @Test
    public void getArrayBracketsPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType[0:4:2]", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][2]\"]", output);
    }

    @Test
    public void getArrayBracketsQuotePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType['0:4:2']", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][2]\"]", output);
    }

    @Test
    public void getArrayQuotePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.'0:4:2'", "PATH");
        assertEquals("[\"$['ArrayType'][0]\",\"$['ArrayType'][2]\"]", output);
    }

    @Test
    public void onlyArrayRangePATH() throws IOException {
        String output = buildJsonPath(sourceD,"0:0:2", "PATH");
        assertEquals("[\"$[0]\",\"$[2]\",\"$[4]\",\"$[6]\"]", output);
    }

    @Test
    public void onlyDotPATH() throws IOException {
        String output = buildJsonPath(sourceA,".", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void nameWithDotPATH() throws IOException {
        String output = buildJsonPath(sourceC,"$.nameWith.Dot", "PATH"); //Though nameWith.Dot exists, instructions treat it as two different words.
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void nameWithDotQuotePATH() throws IOException {
        String output = buildJsonPath(sourceC,"$.'nameWith.Dot'", "PATH"); //Quotations don't affect how it is read.
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void nameWithDotBracketPATH() throws IOException {
        String output = buildJsonPath(sourceC,"$[nameWith.Dot]", "PATH"); //Dots within brackets are treated as part of the word
        assertEquals("[\"$['nameWith.Dot']\"]", output);
    }

    @Test
    public void nameWithDotBracketQuotePATH() throws IOException {
        String output = buildJsonPath(sourceC,"$['nameWith.Dot']", "PATH"); //Dots within brackets are treated as part of the word
        assertEquals("[\"$['nameWith.Dot']\"]", output);
    }

    @Test
    public void onlyDoubleDotPATH() throws IOException {
        String output = buildJsonPath(sourceA,"..", "PATH"); //Reads as $[]..
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarAllPATH() throws IOException {
        String output = buildJsonPath(sourceA, "$..", "PATH"); //Reads as $[]..
        assertEquals("[\"$\",\"$['NullType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\",\"$['ObjectType2']['InnerValue2']\",\"$['ArrayType2'][2]\"]", output);
    }

    @Test
    public void dollarAllBracketsPATH() throws IOException {
        String output = buildJsonPath(sourceA, "$[[", "PATH"); //Reads as $[]..
        assertEquals("[\"$\",\"$['NullType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\",\"$['ObjectType2']['InnerValue2']\",\"$['ArrayType2'][2]\"]", output);
    }

    @Test
    public void dollarAllBracketsQuotesPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$['['", "PATH"); //Reads as $[]..
        assertEquals("[\"$\",\"$['NullType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\",\"$['ObjectType2']['InnerValue2']\",\"$['ArrayType2'][2]\"]", output);
    }

    @Test
    public void dollarAllBracketsClosedPATH() throws IOException {
        String output = buildJsonPath(sourceC, "$[[]", "PATH"); //Reads as $.[]
        assertEquals("[\"$['']\"]", output);
    }

    @Test
    public void dollarAllGetPATH() throws IOException {
        String output = buildJsonPath(sourceA, "$..InnerValue2", "PATH"); //Reads as $.[]
        assertEquals("[\"$['ObjectType']['InnerValue2']\",\"$['ObjectType2']['InnerValue2']\"]", output);
    }

    @Test
    public void dollarAllGetBracketsPATH() throws IOException {
        String output = buildJsonPath(sourceA, "$[[InnerValue2]", "PATH"); //Reads as $.[]
        assertEquals("[\"$['ObjectType']['InnerValue2']\",\"$['ObjectType2']['InnerValue2']\"]", output);
    }

    @Test
    public void dollarAllGetBracketsQuotePATH() throws IOException {
        String output = buildJsonPath(sourceA, "$['['InnerValue2']", "PATH"); //Reads as $.[]
        assertEquals("[\"$['ObjectType']['InnerValue2']\",\"$['ObjectType2']['InnerValue2']\"]", output);
    }

    @Test
    public void dollarAllGetBracketDotPATH() throws IOException {
        String output = buildJsonPath(sourceA, "$[.InnerValue2", "PATH"); //Reads as $.[]
        assertEquals("[\"$['ObjectType']['InnerValue2']\",\"$['ObjectType2']['InnerValue2']\"]", output);
    }

    @Test
    public void dollarAllGetBracketDotClosedPATH() throws IOException { //Treated as single word
        String output = buildJsonPath(sourceA, "$[.InnerValue2]", "PATH"); //Reads as $.[]
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarAllGetGetPATH() throws IOException {
        String output = buildJsonPath(sourceA, "$..InnerValue2.InnerValue3", "PATH"); //Reads as $.[]
        assertEquals("[\"$['ObjectType2']['InnerValue2']['InnerValue3']\"]", output);
    }

    @Test
    public void dollarAllGetGetBracketsPATH() throws IOException {
        String output = buildJsonPath(sourceA, "$[[InnerValue2][InnerValue3]", "PATH"); //Reads as $.[]
        assertEquals("[\"$['ObjectType2']['InnerValue2']['InnerValue3']\"]", output);
    }

    @Test
    public void dollarAllGetGetBracketsMiddleDotPATH() throws IOException { //Interprets it as whole word
        String output = buildJsonPath(sourceA, "$[[InnerValue2.InnerValue3]", "PATH"); //Reads as $.[]
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarGetStringIndexPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.StringType.4", "PATH"); //Script needs brackets around it, or quotes.
        assertEquals("[\"$['StringType'][4]\"]", output);
    }

    @Test
    public void onlyWildPATH() throws IOException {
        String output = buildJsonPath(sourceA, "*", "PATH");
        assertEquals("[\"$['StringType']\",\"$['NumberType']\",\"$['BooleanType']\",\"$['NullType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\"]", output);
    }

    @Test
    public void dollarWildPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.*", "PATH");
        assertEquals("[\"$['StringType']\",\"$['NumberType']\",\"$['BooleanType']\",\"$['NullType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\"]", output);
    }

    @Test
    public void dollarWildBracketPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$[*", "PATH");
        assertEquals("[\"$['StringType']\",\"$['NumberType']\",\"$['BooleanType']\",\"$['NullType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\"]", output);
    }

    @Test
    public void dollarWildBracketPairPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$[*]", "PATH");
        assertEquals("[\"$['StringType']\",\"$['NumberType']\",\"$['BooleanType']\",\"$['NullType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\"]", output);
    }

    @Test
    public void dollarWildBracketQuotePairPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$['*']", "PATH");
        assertEquals("[\"$['StringType']\",\"$['NumberType']\",\"$['BooleanType']\",\"$['NullType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\"]", output);
    }

    @Test
    public void dollarWildQuotePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.'*'", "PATH");
        assertEquals("[\"$['StringType']\",\"$['NumberType']\",\"$['BooleanType']\",\"$['NullType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\"]", output);
    }

    @Test
    public void dollarWildGetUpperPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.*.NumberPATH", "PATH"); //Since NumberPATH was on the original top layer, it no longer exists.
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarWildGetInnerValue() throws IOException {
        String output = buildJsonPath(sourceA,"$.*.InnerValue4", "PATH"); //Since innerPATH4 was in an object, it can be found.
        assertEquals("[\"$['ObjectType2']['InnerValue4']\"]", output);
    }

    @Test
    public void dollarAllWildPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$..*", "PATH"); //Since innerPATH4 was in an object, it can be found.
        assertEquals("[\"$['StringType']\",\"$['NumberType']\",\"$['BooleanType']\",\"$['NullType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\",\"$['ObjectType']['InnerValue1']\",\"$['ObjectType']['InnerValue2']\",\"$['ObjectType']['InnerValue3']\",\"$['ArrayType'][0]\",\"$['ArrayType'][1]\",\"$['ArrayType'][2]\",\"$['ArrayType'][3]\",\"$['ArrayType'][4]\",\"$['ArrayType'][5]\",\"$['ArrayType'][6]\",\"$['ObjectType2']['InnerValue1']\",\"$['ObjectType2']['InnerValue2']\",\"$['ObjectType2']['InnerValue4']\",\"$['ArrayType2'][0]\",\"$['ArrayType2'][1]\",\"$['ArrayType2'][2]\",\"$['ObjectType2']['InnerValue2']['InnerValue3']\",\"$['ArrayType2'][2]['InnerValue1']\"]", output);
    }

    @Test
    public void dollarWildAllPATH() throws IOException {
        String output = buildJsonPath(sourceA, "$.*..", "PATH"); //Since innerPATH4 was in an object, it can be found.
        assertEquals("[\"$['StringType']\",\"$['NumberType']\",\"$['BooleanType']\",\"$['NullType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\",\"$['ObjectType2']['InnerValue2']\",\"$['ArrayType2'][2]\"]", output);
    }

    @Test
    public void onlyScriptPATH() throws IOException {
        String output = buildJsonPath(sourceC,"(@.length-1)", "PATH"); //Script needs brackets around it, or quotes.
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void onlyScriptQuotesPATH() throws IOException {
        String output = buildJsonPath(sourceC,"'(@.length-1)'", "PATH"); //Reads as $[].Script. As script is String, returns last character.
        assertEquals("[\"$[''][4]\"]", output);
    }

    @Test
    public void onlyScriptBracketsPATH() throws IOException {
        String output = buildJsonPath(sourceC,"[(@.length-1)]", "PATH"); //Script needs brackets around it, or quotes.
        assertEquals("[\"$[''][4]\"]", output);
    }

    @Test
    public void onlyScriptQuoteAndBracketPATH() throws IOException {
        String output = buildJsonPath(sourceC,"[(@.length-1)'", "PATH"); //Script needs brackets around it, or quotes.
        assertEquals("[\"$[''][4]\"]", output);
    }

    @Test
    public void dollarGetScriptDotPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType.(@.length-1)", "PATH"); //Script needs brackets around it, or quotes.
        assertEquals("NO MATCHES", output);
    }


    @Test
    public void dollarGetScriptBracketPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType[(@.length-1)]", "PATH"); //Script needs brackets around it, or quotes.
        assertEquals("[\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void dollarGetScriptQuotesPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(@.length-1)'", "PATH"); //Script needs brackets around it, or quotes.
        assertEquals("[\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void dollarAllScriptPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.'(@.length-1)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarGetAllScriptPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(@.length-1)'", "PATH");
        assertEquals("[\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void dollarWildScriptPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.*'(@.length-1)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarWildAllScriptPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.*.'(@.length-1)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarGetScriptFollowUpPATH() throws IOException { //Ignores follow-up without dot
        String output = buildJsonPath(sourceA,"$.ArrayType2'(@.length-1)'InnerValue1", "PATH");
        assertEquals("[\"$['ArrayType2'][2]\"]", output);
    }

    @Test
    public void dollarGetScriptFollowUpDotPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType2'(@.length-1)'.InnerValue1", "PATH");
        assertEquals("[\"$['ArrayType2'][2]['InnerValue1']\"]", output);
    }

    @Test
    public void dollarGetScriptFollowUpBracketsPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType2'(@.length-1)'[InnerValue1]", "PATH");
        assertEquals("[\"$['ArrayType2'][2]['InnerValue1']\"]", output);
    }

    @Test
    public void dollarGetScriptFollowUpWildPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType2'(@.length-1)'.*", "PATH");
        assertEquals("[\"$['ArrayType2'][2]['InnerValue1']\"]", output);
    }

    @Test
    public void dollarAllScriptFollowUpWildPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType2'(@.length-1)'.*", "PATH");
        assertEquals("[\"$['ArrayType2'][2]['InnerValue1']\"]", output);
    }

    @Test
    public void allScriptLengthPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$..'(@.length-1)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getArrayAllScriptLengthPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType..'(@.length-1)'", "PATH");
        assertEquals("[\"$['ArrayType'][6]\"]", output);
    }

    @Test
    public void scriptBadSpacingPATH() throws IOException { //Script ignores most bad spacing
        String output = buildJsonPath(sourceA,"$.ArrayType'(      @      .length      -      3      )'", "PATH");
        assertEquals("[\"$['ArrayType'][4]\"]", output);
    }

    @Test
    public void scriptBadSpacingStartCommaPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'   (@.length-4)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void scriptBadSpacingBackCommaPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(@.length-4)   '", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void scriptInvalidPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(@.invalid-3)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getNoAtSymbolPATH() throws IOException { //In example, defaults length to 5. Ours does not do this.
        String output = buildJsonPath(sourceA,"$.ArrayType'(length-3)'", "PATH");
        assertEquals("[\"$['ArrayType'][4]\"]", output);
    }

    @Test
    public void scriptLengthMultipleEquationsPATH() throws IOException { //Example allowed multiple equations, ours does not (What reason is there to have multiple?)
        String output = buildJsonPath(sourceA,"$.ArrayType'(length-3+2)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void scriptLengthExceededPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(length+99999)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void scriptLengthBelowZeroPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(length-99999)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void scriptMultiplyPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(length*0.25)'", "PATH");
        assertEquals("[\"$['ArrayType'][1]\"]", output);
    }

    @Test
    public void scriptDividePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'(length/2)'", "PATH");
        assertEquals("[\"$['ArrayType'][3]\"]", output);
    }










    @Test
    public void onlyFilterPATH() throws IOException {
        String output = buildJsonPath(sourceA, "?(@ )", "PATH"); //Acts like wild, but does not retrieve PATHs with JsonNull
        assertEquals("[\"$['StringType']\",\"$['NumberType']\",\"$['BooleanType']\",\"$['ObjectType']\",\"$['ArrayType']\",\"$['ObjectType2']\",\"$['ArrayType2']\"]", output);
    }

    @Test
    public void onlyFilterKeywordPATH() throws IOException {
        String output = buildJsonPath(sourceD,"$.6'?(@.ITEM1)'", "PATH"); //Cannot read PATHs.
        assertEquals("[\"$[6][0]\"]", output);
    }

    @Test
    public void onlyFilterKeywordDotPATH() throws IOException {
        String output = buildJsonPath(sourceD,"$.6.'?(@.ITEM1)'", "PATH"); //Cannot read PATHs.
        assertEquals("[\"$[6][0]\"]", output);
    }

    @Test
    public void filterNegatePATH() throws IOException { //INOPERABLE RIGHT NOW FOR SOURCE D
        String output = buildJsonPath(sourceD,"$'?(!@.Object2)'", "PATH");
        assertEquals("[\"$[3]\"]", output);
    }

    @Test
    public void filterEqualsPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year == 2022)'","PATH");
        assertEquals("[\"$['library'][1]\"]", output);
    }

    @Test
    public void filterNotEqualsPATH() throws IOException {
        String output = buildJsonPath(sourceB, "$.library'?(@.available != true)'", "PATH");
        assertEquals("[\"$['library'][2]\",\"$['library'][3]\",\"$['library'][5]\"]", output);
    }

    @Test
    public void filterLessThanPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year > 2020)'","PATH");
        assertEquals("[\"$['library'][1]\",\"$['library'][5]\"]", output);
    }

    @Test
    public void filterLessThanEqualsPATH() throws IOException {
        String output = buildJsonPath(sourceB, "$.library'?(@.year >= 2020)'", "PATH");
        assertEquals("[\"$['library'][1]\",\"$['library'][5]\",\"$['library'][6]\"]", output);
    }

    @Test
    public void filterGreaterThanPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year < 1987)'","PATH");
        assertEquals("[\"$['library'][3]\",\"$['library'][7]\"]", output);
    }

    @Test
    public void filterGreaterThanEqualsPATH() throws IOException {
        String output = buildJsonPath(sourceB, "$.library'?(@.year <= 1987)'", "PATH");
        assertEquals("[\"$['library'][0]\",\"$['library'][3]\",\"$['library'][7]\"]", output);
    }

    @Test
    public void filterRegexPATH() throws IOException {
        String output = buildJsonPath(sourceB, "$.library'?(@.title =~ .*the.*)'", "PATH");
        assertEquals("[\"$['library'][0]\",\"$['library'][5]\"]", output);
    }


    @Test
    public void filterInPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.genre in ['food', 'history'])'","PATH");
        assertEquals("[\"$['library'][3]\",\"$['library'][4]\"]", output);
    }

    @Test
    public void filterNotInPATH() throws IOException {
        String output = buildJsonPath(sourceB, "$.library'?(@.genre nin ['food', 'history'])'", "PATH");
        assertEquals("[\"$['library'][0]\",\"$['library'][1]\",\"$['library'][2]\",\"$['library'][5]\",\"$['library'][6]\",\"$['library'][7]\"]", output);
    }

    @Test
    public void filterSizeStringPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.title size 14)'","PATH");
        assertEquals("[\"$['library'][1]\"]", output);
    }

    @Test
    public void filterSizeArrayPATH() throws IOException {
        String output = buildJsonPath(sourceD,"$.'?(@ size 3)'", "PATH");
        assertEquals("[\"$[5]\",\"$[6][1]['ITEM2']\"]", output);
    }

    @Test
    public void filterContainsStringPATH() throws IOException {
        String output = buildJsonPath(sourceB, "$.library'?(@.title contains the)'", "PATH");
        assertEquals("[\"$['library'][0]\",\"$['library'][5]\"]", output);
    }

    @Test
    public void filterContainsArrayPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$'?(@ contains 7)'", "PATH");
        assertEquals("[\"$['ArrayType']\"]", output);
    }

    @Test
    public void filterEmptyTruePATH() throws IOException {
        String output = buildJsonPath(sourceC,"$'?(@ empty true)'", "PATH");
        assertEquals("[\"$['empty object']\",\"$['empty array']\",\"$['empty word']\"]", output);
    }

    @Test
    public void filterEmptyFalsePATH() throws IOException {
        String output = buildJsonPath(sourceC, "$'?(@ empty false)'", "PATH");
        assertEquals("[\"$['nameWith.Dot']\",\"$['backSlash\\\\']\",\"$[';']\",\"$['']\",\"$['[']\",\"$[']']\",\"$['0']\",\"$['$.']\",\"$['2:3:4']\",\"$['1:1:1:1']\",\"$['keyword']\",\"$['filledObject']\",\"$['filledobject']\"]", output);
    }

    @Test
    public void filterEmptyInvalidPATH() throws IOException {
        String output = buildJsonPath(sourceC,"$'?(!@ empty yes)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterANDPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.year > 2000 && @.genre != 'non-fiction')", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterORPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.year < 1950 || @.genre == 'non-fiction')", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterANDplusORPATH() throws IOException { //OR Stronger than AND
        String output = buildJsonPath(sourceB,"$.library.?(@.available == false && @.genre == 'fantasy' || @.year == 0)","PATH");
        assertEquals("NO MATCHES", output);
    }


    @Test
    public void filterGetInnerValue() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.available == false).year", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterGetUpperPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.available == false).2", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterGetInvalidPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.INVALID == null)", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterAllPATH() throws IOException {
        String output = buildJsonPath(sourceB, "$.library.?(@ )..age rating", "PATH");
        assertEquals("[\"$['library'][0]['age rating']\",\"$['library'][1]['age rating']\",\"$['library'][2]['age rating']\",\"$['library'][3]['age rating']\",\"$['library'][4]['age rating']\",\"$['library'][5]['age rating']\",\"$['library'][6]['age rating']\",\"$['library'][7]['age rating']\"]", output);
    }

    @Test
    public void filterWildPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library.?(@.title).*", "PATH");
        assertEquals("NO MATCHES", output);
    }






    @Test
    public void onlyCommaPATH() throws IOException {
        String output = buildJsonPath(sourceA,",", "PATH"); //Reads as $[],
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterCommaBlankPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year == 3000)',", "PATH");
        assertEquals("[\"$['library'][5]\"]", output);
    }

    @Test
    public void filterCommaKeyPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year == 3000)',genre", "PATH");
        assertEquals("[\"$['library'][5]\"]", output);
    }

    @Test
    public void filterCommaDotKeyPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year == 3000)',.genre", "PATH");
        assertEquals("[\"$['library'][5]['genre']\"]", output);
    }

    @Test
    public void filterCommaAllPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year == 3000)',..age rating", "PATH");
        assertEquals("[\"$['library'][5]['age rating']\"]", output);
    }

    @Test
    public void filterCommaWildPATH() throws IOException {
        String output = buildJsonPath(sourceB, "$.library'?(@.year == 3000)',.*", "PATH");
        assertEquals("[\"$['library'][5]['title']\",\"$['library'][5]['year']\",\"$['library'][5]['genre']\",\"$['library'][5]['age rating']\",\"$['library'][2]['available']\"]", output);
    }

    @Test
    public void filterScriptPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ArrayType'?(@ )''(@.length-1)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterFilterPATH() throws IOException { //SOURCE D IS INOPERABLE RIGHT NOW
        String output = buildJsonPath(sourceD,"$.'?(@.ITEM1)''?(@.ITEM2)'", "PATH");
        assertEquals("[\"$[6][0]['ITEM1']\"]", output);
    }

    @Test
    public void allFilterPATH() throws IOException {
        String output = buildJsonPath(sourceB, "$.library.'?(@.available != false)'", "PATH");
        assertEquals("[\"$['library'][0]\",\"$['library'][1]\",\"$['library'][4]\",\"$['library'][6]\",\"$['library'][7]\",\"$['library'][0]['title']\",\"$['library'][0]['year']\",\"$['library'][0]['genre']\",\"$['library'][0]['age rating']\",\"$['library'][0]['available']\",\"$['library'][1]['title']\",\"$['library'][1]['year']\",\"$['library'][1]['genre']\",\"$['library'][1]['age rating']\",\"$['library'][2]['title']\",\"$['library'][2]['year']\",\"$['library'][2]['genre']\",\"$['library'][2]['age rating']\",\"$['library'][2]['available']\",\"$['library'][3]['title']\",\"$['library'][3]['year']\",\"$['library'][3]['genre']\",\"$['library'][3]['age rating']\",\"$['library'][4]['title']\",\"$['library'][4]['year']\",\"$['library'][4]['genre']\",\"$['library'][4]['age rating']\",\"$['library'][5]['title']\",\"$['library'][5]['year']\",\"$['library'][5]['genre']\",\"$['library'][5]['age rating']\",\"$['library'][6]['title']\",\"$['library'][6]['year']\",\"$['library'][6]['genre']\",\"$['library'][6]['age rating']\",\"$['library'][7]['title']\",\"$['library'][7]['year']\",\"$['library'][7]['genre']\",\"$['library'][7]['age rating']\"]", output);
    }

    @Test
    public void allFilterTriplePATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library..'?(@.available != false)'", "PATH");
        assertEquals("[\"$['library'][0]\",\"$['library'][1]\",\"$['library'][4]\",\"$['library'][6]\",\"$['library'][7]\",\"$['library'][0]['title']\",\"$['library'][0]['year']\",\"$['library'][0]['genre']\",\"$['library'][0]['age rating']\",\"$['library'][0]['available']\",\"$['library'][1]['title']\",\"$['library'][1]['year']\",\"$['library'][1]['genre']\",\"$['library'][1]['age rating']\",\"$['library'][2]['title']\",\"$['library'][2]['year']\",\"$['library'][2]['genre']\",\"$['library'][2]['age rating']\",\"$['library'][2]['available']\",\"$['library'][3]['title']\",\"$['library'][3]['year']\",\"$['library'][3]['genre']\",\"$['library'][3]['age rating']\",\"$['library'][4]['title']\",\"$['library'][4]['year']\",\"$['library'][4]['genre']\",\"$['library'][4]['age rating']\",\"$['library'][5]['title']\",\"$['library'][5]['year']\",\"$['library'][5]['genre']\",\"$['library'][5]['age rating']\",\"$['library'][6]['title']\",\"$['library'][6]['year']\",\"$['library'][6]['genre']\",\"$['library'][6]['age rating']\",\"$['library'][7]['title']\",\"$['library'][7]['year']\",\"$['library'][7]['genre']\",\"$['library'][7]['age rating']\"]", output);
    }

    @Test
    public void filterWithNullPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$'?(@.InnerValue1)'.InnerValue1", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterTooManyItemsPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.available != true false)'", "PATH");
        assertEquals("NO MATCHES", output);
    }
    @Test
    public void filterNoInstructionPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?PATH()'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterNoPATHPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.title ==)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterTooManySpacesPATH() throws IOException {
        String output = buildJsonPath(sourceB, "$.library'?(        @.year         >         2900         )'", "PATH");
        assertEquals("[\"$['library'][5]\"]", output);
    }

    @Test
    public void filterNothingFollowORPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.title ||)'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void filterNothingFollowANDPATH() throws IOException {
        String output = buildJsonPath(sourceB,"$.library'?(@.year > 2000 && )'", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getCommaBlankPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.StringType,", "PATH");
        assertEquals("[\"$['StringType']\"]", output);
    }
    @Test
    public void getCommaSinglePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.StringType,NumberType", "PATH");
        assertEquals("[\"$['StringType']\",\"$['NumberType']\"]", output);
    }

    @Test
    public void getCommaMultiPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.NumberType,NullType,BooleanType", "PATH");
        assertEquals("[\"$['NumberType']\",\"$['NullType']\",\"$['BooleanType']\"]", output);
    }

    @Test
    public void getCommaDuplicatePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.NullType,NullType", "PATH");
        assertEquals("[\"$['NullType']\",\"$['NullType']\"]", output);
    }

    @Test
    public void getInnerValueCommaGetPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.BooleanType,ArrayType.2", "PATH");
        assertEquals("[\"$['BooleanType']\",\"$['ArrayType'][2]\"]", output);
    }

    @Test
    public void getCommaGetInnerValuePATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.InnerValue1,InnerValue3", "PATH");
        assertEquals("[\"$['ObjectType']['InnerValue1']\",\"$['ObjectType']['InnerValue3']\"]", output);
    }

    @Test
    public void getCommaGetUpperPATHPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.InnerValue3,NumberType", "PATH");
        assertEquals("[\"$['ObjectType']['InnerValue3']\"]", output);
    }

    @Test
    public void getCommaDotPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType,.", "PATH");
        assertEquals("[\"$['ObjectType']\"]", output);
    }

    @Test
    public void getCommaDotGetPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType,.NumberType", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void getCommaDotGetValidPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType,.InnerValue2", "PATH");
        assertEquals("[\"$['ObjectType']['InnerValue2']\"]", output);
    }

    @Test
    public void getCommaDollarPATH() throws IOException {
        String output = buildJsonPath(sourceA, "$.NumberType,$", "PATH");
        assertEquals("[\"$['NumberType']\",\"$\"]", output);
    }

    @Test
    public void getCommaDollarInnerKeyPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.InnerValue2,$", "PATH");
        assertEquals("[\"$['ObjectType']['InnerValue2']\",\"$['ObjectType']\"]", output);
    }

    @Test
    public void getCommaDollerUpperKeyPATH() throws IOException {
        String output = buildJsonPath(sourceA,"$.ObjectType.InnerValue2,$.NumberType", "PATH");
        assertEquals("NO MATCHES", output);
    }


    @Test
    public void dollarMissingBracketPATH() throws IOException {
        String output = buildJsonPath(sourceA,"[", "PATH");
        assertEquals("NO MATCHES", output);
    }

    @Test
    public void dollarMissingGetBracketPATH() throws IOException {
        String output = buildJsonPath(sourceA,"[NullType", "PATH");
        assertEquals("NO MATCHES", output);
    }

























}
