package com.azure.json;

import java.io.IOException;
import java.util.Arrays;

public class JsonPointer {
    private JsonElement initialSource;
    private JsonElement pointerResource;

    public JsonPointer(){

    }

    static JsonElement Parse(JsonElement source, String pointer) throws IOException {
        JsonElement selected = source;

        if (!source.isObject() && !source.isArray()){ //Pointer won't work for non-container Json type
            throw new IOException();
        }

        if(pointer.length() == 0){ //Case: Path = "";
            return source;
        } else if (pointer.charAt(0) != '/'){ //Valid JsonPointer requires / at the start.
            throw new IOException();
        } else if (pointer.equals("/")){
            selected = ((JsonObject)source).getProperty("");
            if(selected == null){
                throw new NullPointerException();
            }
        }

        String[] keywords = pointer.split("/");
        for (int i = 1; i < keywords.length; i++){ //Due to nature of split, always detects first item as a entry with no text.

            //First, remove all instances of ~1 and ~0 that already exist.
            for(int j = 0; j < keywords[i].length(); j++){
                if(keywords[i].charAt(j) == '~'){
                    if(j == keywords[i].length()-1 || (keywords[i].charAt(j+1) != '0' && keywords[i].charAt(j+1) != '1')){
                        throw new IOException("~ Symbol must be followed with 0 or 1");
                    }
                }
            }

            keywords[i] = keywords[i].replaceAll("~1", "/");
            keywords[i] = keywords[i].replaceAll("~0", "~");


            if(selected.isObject()){
                selected = ((JsonObject)selected).getProperty(keywords[i]);
                if(selected == null){
                    throw new NullPointerException("Key: " + keywords[i] + " does not exist");
                }
            } else { //Already checked to make sure it is object or array, should only run if is array.
                try{
                    int index = Integer.parseInt(keywords[i]);
                    selected = ((JsonArray)selected).getElement(index);
                } catch (Exception e){
                    e.printStackTrace();
                    throw new IOException("Couldn't parse index");
                }
            }
        }
        return selected;
    }
}
