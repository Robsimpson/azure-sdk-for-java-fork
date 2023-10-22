package com.azure.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class JsonPath {

    static JsonElement ParsePaths(JsonElement sourceElement, JsonElement result, String path) throws IOException {
        JsonElement currentElement = sourceElement;
        JsonArray pathList = new JsonArray();
        //System.out.println("RESULT RECEIVED = " + result);
        //System.out.println("PATHTEST HERE");

        if(result.isArray()){
            ArrayList<JsonElement> paths = new ArrayList<>();
            ArrayList<JsonElement> removedElements = new ArrayList<>();
            ArrayList<String> inputKeys = new ArrayList<>();
            boolean exists = true;

            for(int i = 0; i < ((JsonArray)result).size(); i++){

                inputKeys.add("$");
                while (true){
                    ////System.out.println("CURRENT PATH = " + currentElement);
                    paths.add(currentElement);
                    if(((JsonArray)result).getElement(i) == currentElement){
                        break;
                    } else {
                        removedElements.add(currentElement);
                        ////System.out.println("CURRENT REMOVED ENTRIES = " + removedElements);
                        ////System.out.println("PATHS = " + paths);
                        if(currentElement.isObject()){
                            Set<String> keyset = ((JsonObject)currentElement).getKeys();
                            boolean keyFound = false;
                            for(String key : keyset){
                                if(!removedElements.contains(((JsonObject)currentElement).getProperty(key))){
                                    inputKeys.add("['" + key + "']");
                                    ////System.out.println("Checking key " + key);
                                    currentElement = ((JsonObject)currentElement).getProperty(key);
                                    keyFound = true;
                                    break;
                                }
                            }
                            if(!keyFound){
                                inputKeys.remove(inputKeys.size()-1);
                                paths.remove(paths.size()-1);
                                if(paths.size() == 0){
                                    exists = false;
                                    break;
                                }
                                currentElement = paths.get(paths.size()-1);
                                paths.remove(paths.size()-1);
                            }
                        } else if (currentElement.isArray()){
                            boolean indexFound = false;
                            for(int j = 0; j < ((JsonArray)currentElement).size(); j++){
                                if(!removedElements.contains(((JsonArray)currentElement).getElement(j))){
                                    inputKeys.add("[" + j + "]");
                                    ////System.out.println("Checking index " + j);
                                    currentElement = ((JsonArray)currentElement).getElement(j);
                                    indexFound = true;
                                    break;
                                }
                            }
                            if(!indexFound){
                                inputKeys.remove(inputKeys.size()-1);
                                paths.remove(paths.size()-1);
                                currentElement = paths.get(paths.size()-1);
                                paths.remove(paths.size()-1);
                            }
                        } else {
                            if(inputKeys.size() > 1){
                                inputKeys.remove(inputKeys.size()-1);
                            }
                            paths.remove(paths.size()-1);
                            currentElement = paths.get(paths.size()-1);
                            paths.remove(paths.size()-1);
                            ////System.out.println("current element is now " + currentElement);
                        }
                    }
                }
                if(exists){
                    //System.out.println("FINAL RESULT = " + inputKeys);
                    String combinedAddress = "";
                    for(int k = 0; k < inputKeys.size(); k++){
                        combinedAddress += inputKeys.get(k);
                    }
                    pathList.addElement(new JsonString(combinedAddress));
                    paths.clear();
                    removedElements.clear();
                    inputKeys.clear();
                    currentElement = sourceElement;
                } else {


                    int lastDot = path.lastIndexOf(".");
                    int lastBracket = path.lastIndexOf("[");
                    int lastScript = path.lastIndexOf("[(");
                    //System.out.println(lastDot+"/"+lastBracket+"/"+lastScript);
                    if (lastDot == lastScript+3){
                        lastDot = -1;
                    }
                    //System.out.println(lastDot+"/"+lastBracket+"/"+lastScript);
                    int highest = -1;
                    if(lastDot != -1 && lastBracket != -1) {
                        if (lastDot > lastBracket){
                            highest = lastDot;
                        } else {
                            highest = lastBracket;
                        }
                    } else if (lastDot == -1 && lastBracket != -1){
                        highest = lastBracket;
                    } else if (lastBracket == -1 && lastDot != -1){
                        highest = lastDot;
                    }
                    if(highest == -1 && lastScript != -1){
                        highest = lastScript;
                    } else if (highest != -1 && lastScript != -1 && lastScript > highest){
                        highest = lastScript;
                    }

                    //System.out.println(currentElement.toString() + "!!!");

                    if(highest != -1){
                        String splitPath = path.substring(0, highest);
                        JsonElement splitResult = Parse(sourceElement, splitPath, "VALUE");
                        JsonElement chosenPath = ((JsonArray)Parse(sourceElement, splitPath, "PATH")).getElement(i);
                        inputKeys.add(chosenPath.toString());

                        String stringResult = ((JsonArray)splitResult).getElement(i).toString();
                        String numberWanted = "";
                        if(highest == lastScript && lastScript == lastBracket){
                            int loop = path.indexOf("length")+6;
                            while (loop+1 != path.length() && path.charAt(loop) != ')'){
                                numberWanted += path.charAt(loop);
                                loop++;
                            }
                            int index = getIndex(stringResult.length(), numberWanted);
                            //System.out.println(numberWanted);
                            String letter = "" + stringResult.charAt(index);
                            //System.out.println(letter);
                            if(letter.equals((((JsonArray)result).getElement(i).toString()))){
                                inputKeys.add("[" + index + "]");
                            }
                        } else {
                            int loop = highest+1;
                            if(loop+1 != path.length() && path.charAt(loop+1) == '\''){
                                loop++;
                            }
                            //System.out.println(path.charAt(loop) + " MMM");
                            while (loop != path.length() && path.charAt(loop) != '\'' && path.charAt(loop) != ']'){
                                numberWanted += path.charAt(loop);
                                loop++;
                            }
                            try {
                                //System.out.println(numberWanted);
                                int index = Integer.parseInt(numberWanted);
                                String letter = "" + stringResult.charAt(index);
                                //System.out.println(letter);
                                if(letter.equals((((JsonArray)result).getElement(i).toString()))){
                                    inputKeys.add("[" + index + "]");
                                }
                            } catch (NumberFormatException e){
                                return new JsonString("NO MATCHES");
                            }
                        }

                        String combinedAddress = "";
                        for(int k = 0; k < inputKeys.size(); k++){
                            combinedAddress += inputKeys.get(k);
                        }
                        pathList.addElement(new JsonString(combinedAddress));
                        paths.clear();
                        removedElements.clear();
                        inputKeys.clear();
                        currentElement = sourceElement;
                    }
                }
            }
        } else {
            return result; //Should only be "NO MATCHES" JsonString
        }
        return pathList;
    }


    static int getIndex(int start, String equationText){
        int result = start;
        String equationChars = "+-*/";
        float functionNumber;

        for(int i = 0; i < equationText.length(); i++){
            String choice = "" + equationText.charAt(i);
            String numbersWanted = "";
            if(!equationChars.contains(choice)){
                return -1;
            }
            i++;
            while(i != equationText.length() && !equationChars.contains(equationText.charAt(i)+"")){
                numbersWanted += equationText.charAt(i);
                i++;
            }
            try {
                functionNumber = Float.parseFloat(numbersWanted);
            } catch (NumberFormatException e){
                return -1;
            }

            switch (choice){
                case "+":
                    result = (int)((float)result + functionNumber);
                    break;
                case "-":
                    result = (int)((float)result - functionNumber);
                    break;
                case "*":
                    result = (int)((float)result * functionNumber);
                    break;
                case "/":
                    result = (int)((float)result / functionNumber);
                    break;
                default:
                    return -1;
            }
        }
        if(result >= start){
            return -1;
        } else {
            return result;
        }
    }

    static JsonElement Parse(JsonElement sourceElement, String pathText, String args) throws IOException {
        //System.out.println(sourceElement);
        JsonElement search = sourceElement;
        JsonArray matches = new JsonArray();
        ArrayList<String> instructions = new ArrayList<>();
        String currentKey = "";
        String operation = "G ";

        String startingChar = "";

        //System.out.println("DEBUG - 102: Initial Path:\n" + pathText + "\n");
        if(pathText.contains("\"")){
            throw new NullPointerException(); //Not including ; or " as a potential code safety precaution.
        }



        /*Keywords:
        GET: G
        ALL: A
        WILD: W
        FILTER: F
        SCRIPT: S
        COMMA: C
        */

        if(pathText.equals("")){ //Case: element.jsonPath("");
            return Parse(sourceElement, "$[]", args);
        } else if ((!search.isObject())&&(!search.isArray())){ //Case: element not a container type
            return new JsonString("NO MATCHES");
        }

        instructions.add("G $");

        //Treat semicolon as a left bracket;
        pathText = pathText.replace(";", "[");
        //Treat quotes next to script or filter as brackets
        pathText = pathText.replace("'(", "[(");
        pathText = pathText.replace("'?(", "[?(");
        pathText = pathText.replace(")'", ")]");
        if(pathText.contains("]") && !pathText.contains("[")){
            pathText = "$[" + pathText;
        }



        if(pathText.charAt(0) != '$'){
            if((pathText.charAt(0) == '[')||(pathText.charAt(0) == '.')){ //Case: element.jsonPath("$.");
                pathText = "$[]" + pathText;
            } else { //Case: element.jsonPath("name");
                pathText = "$." + pathText;
            }
        } else {
            if (pathText.equals("$")){ //Case: element.jsonPath("$");
                matches.addElement(sourceElement);
                //System.out.println("MATCHES = " + matches);
                if(args.equals("VALUE")){
                    return matches;
                } else if (args.equals("PATH")){
                    return ParsePaths(sourceElement, matches, pathText);
                } else {
                    throw new IOException("args must be VALUE or PATH");
                }
            } else if ((pathText.charAt(1) != '[')&&(pathText.charAt(1) != '.')){ //Case: element.jsonPath("$name");
                pathText = "$." + pathText;
            }
        }



        ArrayList<Number> startBrackets = new ArrayList<>();
        ArrayList<Number> endBrackets = new ArrayList<>();

        //Need to distinguish between [' and [ for usage with Filters







        //Record locations of brackets
        int startLocation = 0;
        int endLocation = pathText.length();

        if(pathText.contains("[")) {
            while (pathText.contains("]]")) { //Double brackets do nothing as bracket cannot be used within a key (Exception: Filters).
                pathText = pathText.replace("]]", "]");
            }

            while(pathText.indexOf('[', startLocation) != -1){
                startLocation = pathText.indexOf('[', startLocation);
                //System.out.println("START:" + startLocation);
                endLocation = pathText.indexOf(']', startLocation);
                //System.out.println("END: " + endLocation);

                //String startingItem = pathText.substring(startLocation+1, endLocation+1);
                ////System.out.println("STARTING ITEM WAS: " + startingItem);
                if(pathText.contains("(")){
                    if(pathText.lastIndexOf("(", startLocation) != -1
                        && startLocation > pathText.lastIndexOf("(", startLocation)
                        && startLocation < pathText.indexOf(")", startLocation)
                        && (pathText.indexOf("(", startLocation) == -1
                        || pathText.indexOf("(", startLocation) > pathText.indexOf(")", startLocation))
                    ){
                        startLocation = pathText.indexOf(")", startLocation);
                        //System.out.println("BRACKET ( detected.");
                        //System.out.println("NEW STARTLOCATION = " + startLocation);
                        continue;
                    }
                }

                if(endLocation != -1){
                    if(pathText.contains(")")){
                        while(endLocation < pathText.indexOf(")", endLocation)
                        && pathText.lastIndexOf("(", endLocation) != -1
                        && pathText.lastIndexOf("(", endLocation) < endLocation
                        && pathText.lastIndexOf("(", endLocation) > pathText.lastIndexOf(")", endLocation)
                        ){
                            endLocation = pathText.indexOf(")", endLocation);
                            endLocation = pathText.indexOf("]", endLocation);
                            //System.out.println(endLocation);
                            if(endLocation != -1) endLocation--; break;
                        }
                    }

                    if(endLocation != -1){
                        String currentItem = pathText.substring(startLocation+1, endLocation+1);
                        //System.out.println("CURRENT ITEM: " + currentItem);


                        if(!currentItem.contains("[") || (((currentItem.charAt(0) == '?' && currentItem.charAt(1)=='(') || currentItem.charAt(0) == '(') && currentItem.charAt(currentItem.length()-1) == ')')) {
                            //System.out.println("ITEM ADDED");
                            startBrackets.add(startLocation);
                            if(currentItem.charAt(currentItem.length()-1) == ')'){
                                endBrackets.add(endLocation);
                            } else {
                                endBrackets.add(endLocation-1);
                            }

                        }
                    }
                }
                startLocation++;
            }
        }

        //Handle end bracket with no start
        if (pathText.contains("]")){
            int lastBracket = pathText.lastIndexOf("]", pathText.length()-1);
            if(!endBrackets.contains(lastBracket-1)){
                int replacerBracket = pathText.lastIndexOf("[", lastBracket);
                while(!startBrackets.contains(replacerBracket)){ //True if bracket is part of Filter (and possibly script)
                    replacerBracket = pathText.lastIndexOf("[", lastBracket);
                }
                int indexWanted = startBrackets.indexOf(replacerBracket);
                endBrackets.set(indexWanted, lastBracket-1);
            }
        }




        int i = 1; //Since input should always start with $. or $[, we can simply skip over the first character
        while (i < pathText.length()){

            boolean keySpecified = false;
            currentKey = "";




            if(pathText.charAt(i) == '[' || pathText.charAt(i) == '.'){
                if(i + 1 != pathText.length()){ //Assumes that there is another character afterwards.
                    int commaCheck;
                    if(i+1 != pathText.length() && pathText.charAt(i+1) == '\''){
                        commaCheck = 1;
                        //System.out.println(commaCheck);
                        //System.out.println("i = " + pathText.substring(i));
                        //System.out.println(pathText.length());
                        while(i + commaCheck != pathText.length()-1 && pathText.charAt(i + commaCheck) == '\''){
                            commaCheck++;
                            //System.out.println(commaCheck);
                        }
                        commaCheck--;
                        if(commaCheck == 2){
                            //System.out.println("THERE IS TWO COMMAS");
                            if(i+commaCheck+1 != pathText.length() && (pathText.charAt((i+commaCheck+1))=='.' || pathText.charAt((i+commaCheck+1))=='[')){
                                operation = "A";
                                i = i + commaCheck + 1;
                            } else {
                                operation = "G ";
                            }
                        } else if (commaCheck == 1){
                            //System.out.println("THERE IS ONE COMMA");
                            if(i+commaCheck+1 != pathText.length() && (pathText.charAt((i+commaCheck+1))=='.' || pathText.charAt((i+commaCheck+1))=='[')){
                                operation = "A";
                                i = i + commaCheck + 1;
                            } else {
                                operation = "G ";
                            }
                        } else {
                            break;
                        }
                    } else if(pathText.charAt(i+1) == '.' || (pathText.charAt(i+1) == '[')){
                        if(pathText.charAt(i) != '[' || !startBrackets.contains(i)){
                            operation = "A";
                            i++;
                        }
                    } else { //Used to reset operation if not triggered by commaCheck
                        //System.out.println("GGG");
                        operation = "G ";
                    }
                } else {
                    break;
                }

                //System.out.println("CURRENT CHAR = " + i);

                //Check if the upcoming key has already been recorded in the Array entries
                if(pathText.charAt(i) == '[' && startBrackets.contains(i)){
                    //System.out.println("DEBUG - 238: Found index within brackets set");
                    int match = startBrackets.indexOf(i);
                    int endPoint = endBrackets.get(match).intValue();

                    currentKey = pathText.substring(i+1, endPoint+1);
                    //System.out.println(currentKey);

                    //Check if the key is an instance of a script.
                    keySpecified = true;
                    i = endPoint+1;
                    //System.out.println(pathText.charAt(i) + " BUGGER " + i);
                } else { //Not in brackets, so handle manually.
                    i++;
                    while(i != pathText.length()){
                        if(currentKey.contains("?(")){ //FILTER CHECK
                            if(currentKey.charAt(currentKey.length()-1) == ')'){
                                break;
                            } else {
                                currentKey += pathText.charAt(i);
                            }
                        } else if (currentKey.contains("'(")) { //SCRIPT CHECK
                            if (currentKey.charAt(currentKey.length() - 1) == '\'' && currentKey.charAt(currentKey.length() - 2) == ')') {
                                break;
                            } else {
                                currentKey += pathText.charAt(i);
                            }
                        }else if (i+1 < pathText.length() && (pathText.charAt(i) == '[') && (pathText.charAt(i+1) == '(')){
                            break;
                        }else if (i+2 < pathText.length() && (pathText.charAt(i) == '[') && (pathText.charAt(i+1) == '?') && (pathText.charAt(i+2) == '(')){
                            break;
                        } else if (pathText.charAt(i) == '[' || pathText.charAt(i) == '.' || pathText.charAt(i) == ',' || pathText.charAt(i) == ']'){
                            break;
                        } else {
                            currentKey += pathText.charAt(i);
                        }
                        i++;
                    }
                    if(i != pathText.length() && pathText.charAt(i) == '\''){
                        currentKey += pathText.charAt(i);
                    }
                }
                //System.out.println("KEYWORD " + currentKey);
                //System.out.println(keySpecified);



                //If the key starts with commas, remove them.
                boolean commaFirst = false;
                boolean commaLast = false;
                if(currentKey.length() != 0){
                    if(currentKey.charAt(0) == '\''){
                        currentKey = currentKey.substring(1);
                        commaFirst = true;
                    }
                    if(currentKey.length() != 0){
                        if(currentKey.charAt(currentKey.length()-1) == '\''){
                            currentKey = currentKey.substring(0, currentKey.length()-1);
                            commaLast = true;
                        }
                    }
                }

                //System.out.println("DEBUG - 279: Current Input = " + operation + currentKey);





                //Go through the options and determine functionality
                if(currentKey.length() != 0 && currentKey.charAt(0) == '(' && ((commaFirst && commaLast)||(keySpecified))){
                    if (currentKey.charAt(0) == '(') {
                        if(currentKey.charAt(currentKey.length()-1) == ')'){
                            if(operation == "A"){
                                instructions.add(operation);
                            }
                            operation = "S ";
                            instructions.add(operation + currentKey);
                        } else {
                            return new JsonString("NO MATCHES");
                        }
                    }
                } else if (currentKey.equals("*")){
                    if(operation.equals("A")){
                        instructions.add(operation);
                    }
                    operation = "W";
                    instructions.add(operation);
                } else if (currentKey.length() > 1 && currentKey.charAt(0) == '?' && currentKey.charAt(1) == '(') {
                    if (operation.equals("A")) {
                        instructions.add(operation);
                    }
                    if(keySpecified || (commaFirst && commaLast) || currentKey.equals("?(@ )")){
                        instructions.add("F " + currentKey);
                    } else {
                        //System.out.println("HUH1");
                        return new JsonString("NO MATCHES");
                    }

                } else if (operation.equals("A")){
                    if(instructions.size() > 0 && (instructions.get(instructions.size()-1).equals("A"))){
                        instructions.add("G ");
                    }
                    instructions.add("A");
                    //System.out.println("HEY LOOK LISTEN: " + (pathText.length()-i));

                    //System.out.println("HERE ARE THE STATS:");
                    //System.out.println(keySpecified);
                    //System.out.println(currentKey);
                    //System.out.println(pathText.length() - i);
                    if(!currentKey.equals("") || keySpecified){
                        instructions.add("G " + currentKey);
                    }
                } else if (currentKey.equals("") && ((instructions.get(instructions.size()-1) == "C" || ((commaFirst && !commaLast)&&(!keySpecified))))){
                    continue;
                } else {
                    instructions.add(operation + currentKey);
                }







            //How to deal with commas
            } else if (pathText.charAt(i) == ','){
                //System.out.println(pathText);
                if(i + 1 == pathText.length() || (pathText.charAt(i + 1) == '.' || pathText.charAt(i + 1) == '[')){
                    i++;
                    continue;
                }

                if(instructions.get(instructions.size()-1).equals("A") || (instructions.get(instructions.size()-1).equals("G $") && (pathText.charAt(i - 1) == '.' || pathText.charAt(i-1) =='['))){ //If preceeded by $.., treats like like $.[]
                    instructions.add("G ");
                }
                if(!instructions.get(instructions.size()-1).equals("C")){
                    operation = "C";
                    instructions.add(operation);
                }

                //For all cases except for filters
                if(instructions.size() > 1 && instructions.get(instructions.size()-2).charAt(0) != 'F'){
                    pathText = pathText.substring(0, i) + "." + pathText.substring(i+1);

                } else { //Specifically for filters
                    while(i+1 < pathText.length() && pathText.charAt(i) != '.' && pathText.charAt(i) != '['){
                        i++;
                    }
                    if(i+1 != pathText.length()){
                        instructions.remove(instructions.size()-1);
                    }
                }

            } else if (pathText.charAt(i) == ']') {
                i++;
            } else { //Happens for text that follows a ] bracket without leading with . or [
                while(i < pathText.length() && (pathText.charAt(i) != '.' && pathText.charAt(i) != '[')){
                    i++;
                }
            }
        }


        //System.out.println("FINAL INSTRUCTIONS: " + instructions + "\n\n");
        if(instructions.size() == 0){
            return new JsonArray().addElement(sourceElement);
        }

        JsonElement result = getMatches(instructions, sourceElement);
        if(args.equals("VALUE")){
            return result;
        } else if (args.equals("PATH")){
            return ParsePaths(sourceElement, result, pathText);
        } else {
            throw new IOException("args must be VALUE or PATH");
        }
    }

    private static JsonElement getMatches(ArrayList<String> commands, JsonElement sourceElement){
        JsonArray grabbedElement = new JsonArray().addElement(sourceElement);
        JsonElement result;
        JsonArray matches = new JsonArray();
        JsonArray commaMatches = new JsonArray();

        Set<String> keyset;
        ArrayList<String> subCommands = new ArrayList<>();

        //System.out.println("DEBUG - 345: Current SourceElement = " + sourceElement);
        //System.out.println();
        for(int i = 0; i < commands.size(); i++){
            JsonElement currentElement;
            subCommands.clear();
            String address = commands.get(i);

            //System.out.println("Case before switch = " + grabbedElement);
            //System.out.println("OPERATION = " + address.charAt(0));
            //System.out.println("Command = " + commands.get(i));
            switch(address.charAt(0)){
                case 'G': //GET
                    address = address.substring(2);
                    if(address.equals("$")){
                        if(i != 0 && commands.get(i-1).equals("C")){
                            matches = new JsonArray().addElement(sourceElement);
                            break;
                        } else {
                            matches = grabbedElement;
                            break;
                        }
                    } else if (address.contains("@")){
                        return new JsonString("NO MATCHES");
                    }

                    for(int j = 0; j < grabbedElement.size(); j++) {
                        currentElement = grabbedElement.getElement(j);

                        //Check if keyword is length(), the only keyword that does something unique
                        if (address.equals("length()") && (i == commands.size() - 1)) {
                            int length = -1;
                            if (currentElement.isObject()) {
                                length = ((JsonObject) currentElement).getKeys().size();
                            } else if (currentElement.isArray()) {
                                length = ((JsonArray) currentElement).size();
                            } else if (currentElement.isString()) {
                                length = currentElement.toString().length();
                            }
                            if (length != -1) {
                                matches.addElement(new JsonString(String.valueOf(length)));
                            }
                        }

                        if (currentElement.isObject()) {
                            //System.out.println("DEBUG - G1: GET uses Object");
                            keyset = ((JsonObject)currentElement).getKeys();
                            if (keyset.contains(address)) {
                                result = ((JsonObject) currentElement).getProperty(address);
                                matches.addElement(result);
                                //System.out.println("MATCHES MODIFIED G1: " + matches);

                            }
                        } else if (currentElement.isArray()) {
                            //System.out.println("DEBUG - G2: GET uses Array");
                            if (address.contains(":")) {
                                //System.out.println("DEBUG - G3: GET Array Key contains : symbol");
                                int colonCount = 0;
                                for (int x = 0; x < address.length(); x++) {
                                    if (address.charAt(x) == ':') {
                                        colonCount++;
                                    }
                                }
                                if (colonCount >= 3) {
                                    return new JsonString("NO MATCHES");
                                }
                                String[] splitKeyword = address.split(":");
                                //System.out.println(splitKeyword.length);

                                int[] arrayParams = new int[]{0, 0, 0};
                                boolean startSpecified = false;
                                for (int x = 0; x < splitKeyword.length; x++) {

                                    try {
                                        arrayParams[x] = Integer.parseInt(splitKeyword[x]);
                                    } catch (NumberFormatException e) {
                                        if (splitKeyword[x].equals("")) {
                                            if (x == 0) {
                                                startSpecified = true;
                                            } else if (x == 1 && colonCount > 0) {
                                                arrayParams[x] = ((JsonArray) currentElement).size();
                                            }
                                        } else {
                                            //System.out.println("BUGGER 1");
                                            return new JsonString("NO MATCHES");
                                        }
                                    }
                                }

                                if (arrayParams[0] < 0) { //Fix any negative values
                                    arrayParams[0] = ((JsonArray) currentElement).size() + arrayParams[0];
                                    if (arrayParams[0] < 0) {
                                        arrayParams[0] = 0;
                                    }
                                } else if (arrayParams[0] >= ((JsonArray) currentElement).size()) {
                                    return new JsonString("NO MATCHES");
                                }
                                if (arrayParams[1] <= 0) {
                                    arrayParams[1] = ((JsonArray) currentElement).size() + arrayParams[1];
                                    if (arrayParams[1] <= 0) {
                                        return new JsonString("NO MATCHES");// (arrayParam[1] outside bounds of array";
                                    }
                                }
                                if (arrayParams[1] > ((JsonArray) currentElement).size()) {
                                    arrayParams[1] = ((JsonArray) currentElement).size();
                                }
                                if (arrayParams[2] < 0) {
                                    return new JsonString("NO MATCHES");// (arrayParam[2] was negative)";
                                }
                                if (arrayParams[2] == 0) { //If zero, works the same as one.
                                    arrayParams[2]++;
                                }

                                int startValue = arrayParams[0];
                                //System.out.println("DEBUG - G4: Array Values set: Input = " + Arrays.toString(arrayParams));

                                for (int x = startValue; x < arrayParams[1]; x += arrayParams[2]) {
                                    result = ((JsonArray) currentElement).getElement(x);
                                    matches.addElement(result);
                                }

                            } else {
                                //System.out.println("DEBUG - G6: Array did not have : symbols");
                                try {
                                    int selected = Integer.parseInt(address);
                                    //System.out.println(selected);
                                    if (selected >= 0 && selected < ((JsonArray) currentElement).size()) {
                                        result = ((JsonArray) currentElement).getElement(selected);
                                        matches.addElement(result);
                                    }
                                } catch (NumberFormatException ignored) {

                                }

                            }
                        } else if (currentElement.isString()) {
                            try {
                                int selected = Integer.parseInt(address);
                                String jsonText = ((JsonString) currentElement).toString();
                                if (selected < jsonText.length() && selected >= 0) {
                                    String characterFind = "" + jsonText.charAt(selected);
                                    result = new JsonString(characterFind);
                                    matches.addElement(result);
                                }
                            } catch (NumberFormatException f) {

                            }
                        }
                    }
                    //System.out.println("FINAL ITEM = " + matches);
                    grabbedElement = matches;
                    break;


                case 'A': //ALL Cases
                    for(int j = 0; j < grabbedElement.size(); j++) {
                        currentElement = grabbedElement.getElement(j);
                        //System.out.println("DEBUG - A1: ALL Detected");

                        //Get the original element.
                        if(currentElement.isObject()) {
                            Set<String> keys = ((JsonObject)currentElement).getKeys();
                            for(String key: keys){
                                result = ((JsonObject)currentElement).getProperty(key);
                                if(result.isObject() || result.isArray() || result.isNull()){
                                    grabbedElement.addElement(result);
                                }
                            }
                        } else if (currentElement.isArray()){
                            for(int k = 0; k < ((JsonArray)currentElement).size(); k++){
                                result = ((JsonArray)currentElement).getElement(k);
                                if(result.isObject() || result.isArray() || result.isNull()){
                                    grabbedElement.addElement(result);
                                }
                            }
                        } else if (currentElement.isNull()){
                            //System.out.println("DOES THIS ONE RUN?");
                        }

                    }
                    matches = grabbedElement;
                    break;



                case 'W': //WILD. Currently fully operration.
                    for(int j = 0; j < grabbedElement.size(); j++) {
                        currentElement = grabbedElement.getElement(j);
                        //Check if last entry:

                        if(currentElement.isObject()){
                            keyset = ((JsonObject) currentElement).getKeys();
                            for (String key : keyset) {
                                if (commands.get(i).equals("W") || (commands.get(i).equals("W?") && !((JsonObject)currentElement).getProperty(key).isNull())){
                                    matches.addElement(((JsonObject)currentElement).getProperty(key));
                                }
                            }
                        } else if (currentElement.isArray()){
                            for (int x = 0; x < ((JsonArray) currentElement).size(); x++) {
                                matches.addElement(((JsonArray) currentElement).getElement(x));
                            }
                        }
                    }
                    //System.out.println("FINAL ITEM = " + matches);
                    grabbedElement = matches;
                    break;
                case 'S': //SCRIPT
                    for(int j = 0; j < grabbedElement.size(); j++) {
                        currentElement = grabbedElement.getElement(j);

                        //Send script to be parsed
                        address = commands.get(i).substring(2);
                        result = parseScript(address, currentElement);

                        if (result.toString().equals("BAD SCRIPT")){
                            return new JsonString("NO MATCHES");
                        } else if (!result.toString().equals("NO MATCHES")) {
                            matches.addElement(result);
                        }
                    }

                    grabbedElement = matches;
                    break;



                case 'F': //FILTER
                    for(int j = 0; j < grabbedElement.size(); j++){
                        currentElement = grabbedElement.getElement(j);
                        //if(currentElement.isObject() && (i != 0 && commands.get(i-1).charAt(0) != 'W')){
                        //    continue;
                        //}




                        //Convert Filter [?(@ )] into .*, as it makes it more simple to use.
                        if(commands.get(i).equals("F ?(@ )")){
                            commands.remove(i);
                            commands.add(i, "W?");
                            i--;
                            matches = grabbedElement;
                            break;
                        }

                        JsonArray filterMatches = new JsonArray();


                        //Split OR command
                        if(commands.get(i).contains(" || ")){
                            String[] multiOR = commands.get(i).split("\\|\\|");
                            for (int x = 0; x < multiOR.length; x++) {
                                if (x == 0) {
                                    multiOR[x] = multiOR[x] + ")]";
                                } else if (x == multiOR.length - 1) {
                                    multiOR[x] = "F ?(" + multiOR[x];
                                } else {
                                    multiOR[x] = "F ?(" + multiOR[x] + ")";
                                }
                                subCommands.add(multiOR[x]);
                            }
                        } else {
                            subCommands.add(commands.get(i));
                        }


                        boolean nullFail = false;
                        //System.out.println("FILTER CURRENTELEMENT = " + currentElement);
                        //System.out.println(subCommands);
                        //Parse Filter
                        for(int k = 0; k < subCommands.size(); k++){
                            result = parseFilter(subCommands.get(k), currentElement);
                            if(result.toString().equals("NullFail")){
                                nullFail = true;
                                break;
                            }
                            if(!result.toString().equals("NO MATCHES")){
                                filterMatches.addElement(result);
                            }
                        }
                        if (nullFail) {
                            //System.out.println("NULL FAIL TRIGGERED");
                            break;
                        }

                        //Add all filtered matches into matches, avoiding duplication

                        if(filterMatches.size() != 0){
                            //System.out.println("FILTER MATCHES WAS NOT 0");
                            for(int n = 0; n < filterMatches.size(); n++){
                                //System.out.println("WHat is it? " + filterMatches);
                                JsonArray filteredResult = (JsonArray) filterMatches.getElement(n);
                                //System.out.println("FILTERED ENTRY: " + filteredResult);
                                for(int p = 0; p < filteredResult.size(); p++){
                                    if(currentElement.isObject()) {
                                        //System.out.println("CURRENT ELEMENT WAS OBJECT");
                                        keyset = ((JsonObject)currentElement).getKeys();
                                        for(String key: keyset) {
                                            boolean matchesContainsResult = false;
                                            for(int x = 0; x < filteredResult.size(); x++){
                                                //System.out.println("F: " + filteredResult.getElement(x) + "\nC: " +  ((JsonObject)currentElement).getProperty(key));
                                                if (filteredResult.getElement(x) == ((JsonObject)currentElement).getProperty(key)){
                                                    //System.out.println("OBJECT: FOUND FILTERED RESULT");
                                                    for (int y = 0; y < matches.size(); y++) {
                                                        if (matches.getElement(y) == ((JsonObject) currentElement).getProperty(key)) {
                                                            matchesContainsResult = true;
                                                            //System.out.println("OBJECT: MATCHES ALREADY HAS THIS ITEM");
                                                        }
                                                    }
                                                    if (!matchesContainsResult) {
                                                        matches.addElement(((JsonObject) currentElement).getProperty(key));
                                                        //System.out.println("MATCHES MODIFIED F1: " + matches);
                                                    }
                                                }
                                            }
                                        }
                                    } else if (currentElement.isArray()){
                                        //System.out.println("CURRENT ELEMENT WAS ARRAY");
                                        for(int q = 0; q < ((JsonArray)currentElement).size(); q++){
                                            boolean matchesContainsResult = false;
                                            for(int x = 0; x < filteredResult.size(); x++) {
                                                //System.out.println("F: " + filteredResult.getElement(x) + "\nC: " +  ((JsonArray)currentElement).getElement(q));
                                                if (filteredResult.getElement(x) == ((JsonArray) currentElement).getElement(q)) {
                                                    //System.out.println("ARRAY FOUND A MATCH");
                                                    for(int y = 0; y < matches.size(); y++) {
                                                        if (matches.getElement(y) == ((JsonArray) currentElement).getElement(q)) {
                                                            //System.out.println("MATCHES ALREADY HAS THIS ARRAY");
                                                            matchesContainsResult = true;
                                                        }
                                                    }
                                                    if(!matchesContainsResult) {
                                                        matches.addElement(((JsonArray) currentElement).getElement(q));
                                                        //System.out.println("MATCHES MODIFIED F2: " + matches);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //System.out.println("MATCHES AFTER FILTERING = " + matches);

                        //After all matches have been acquired, organise them into the correct order;
                        JsonArray reorganizedMatches = new JsonArray();
                        if(currentElement.isObject()){
                            keyset = ((JsonObject)currentElement).getKeys();
                            for(String key: keyset){
                                result = ((JsonObject)currentElement).getProperty(key);
                                for(int y = 0; y < matches.size(); y++){
                                    if(matches.getElement(y) == result){
                                        reorganizedMatches.addElement(result);
                                    }
                                }
                            }
                        } else if (currentElement.isArray()){
                            for(int x = 0; x < ((JsonArray)currentElement).size(); x++){
                                result = ((JsonArray)currentElement).getElement(x);
                                for(int y = 0; y < matches.size(); y++){
                                    if(matches.getElement(y) == result){
                                        reorganizedMatches.addElement(result);
                                    }
                                }
                            }
                        } else {
                            //System.out.println("POOPY BUTTS");
                        }

                        //System.out.println("THE REORGANIZED RESULTS:\n" + reorganizedMatches);
                        if(reorganizedMatches.size() != 0 && !(commands.get(i - 1).equals("A"))){
                            matches = reorganizedMatches;
                        } else {
                            //System.out.println("WHAT?");
                            for(int x = 0; x < reorganizedMatches.size(); x++){
                                boolean matchFound = false;
                                for (int y = 0; y < matches.size(); y++){
                                    if(matches.getElement(y) == reorganizedMatches.getElement(x)){
                                        matchFound = true;
                                    }
                                }
                                if(!matchFound){
                                    matches.addElement(reorganizedMatches.getElement(x));
                                }
                            }
                        }
                    }

                    grabbedElement = matches;
                    break;



                case 'C': //COMMA
                    //Transfer all elements into a safe area
                    if(commands.get(i - 1).equals("W?") || commands.get(i - 1).charAt(0) == 'F' || i+1 == commands.size()){
                        //System.out.println("YAY I AM READING RIGHT!!!");
                        matches = grabbedElement;
                    } else {
                        //System.out.println("COMMAND TEST: " + commands.get(i - 1));
                        for(int j = 0; j < grabbedElement.size(); j++){
                            commaMatches.addElement(grabbedElement.getElement(j));
                        }

                        //Modify commands to equate to current situation.
                        //System.out.println("COMMANDS BEFORE EDIT = " + commands);
                        commands.remove(i);
                        if(i > 0){
                            commands.remove(i-1);
                        }

                        if(i < commands.size() && commands.get(i-1).equals("G $")){
                            commands.remove(i-1);
                            commaMatches = new JsonArray();
                        }

                        //System.out.println("COMMANDS AFTER EDIT = " + commands);
                        i = -1;
                        grabbedElement = new JsonArray().addElement(sourceElement);
                    }


                    break;
                case '!': //Invalid
                    //System.out.println("DEBUG - 358: Unwanted Text Detected" );
                    break;
                default:
                    throw new NullPointerException("No other letter should appear here!");
            }
            //System.out.println("When leaving, matches had: " + matches);
            if(matches.size() != 0 && i+1 == commands.size()){
                if(commaMatches.size() != 0){
                    for(int j = 0; j < matches.size(); j++){
                        commaMatches.addElement(matches.getElement(j));
                    }
                    matches = commaMatches;
                }

                return matches;
            }
            matches = new JsonArray();

        }
        if(commaMatches.size() != 0){
            for(int j = 0; j < matches.size(); j++){
                commaMatches.addElement(matches.getElement(j));
            }
            matches = commaMatches;
        }


        if (matches.size() == 0){
            return new JsonString("NO MATCHES");
        } else {
            return matches;
        }
    }

    private static JsonElement parseScript(String script, JsonElement search){
        //Ensure that script has the entire item
        String endSymbols = "+-/*+&";
        //System.out.println("SCRIPT ORIGINAL = " + script);
        //script = script.substring(1, script.length()-1);
        while(script.contains("( ")){
            script = script.replace("( ", "(");
        }
        while(script.contains(" )")){
            script = script.replace(" )", ")");
        }
        while(script.contains("@ ")){
            script = script.replace("@ ", "@");
        }
        while(script.contains(". ")){
            script = script.replace(". ", ".");
        }
        while(script.contains(" [")){
            script = script.replace(" [", "[");
        }
        while(script.contains("] ")){
            script = script.replace("] ", "]");
        }
        for (int i = 0; i < script.length(); i++){
            if(endSymbols.contains((""+script.charAt(i)))){
                while (script.contains(script.charAt(i) + " ")){
                    script = script.replace(script.charAt(i) + " ", script.charAt(i)+"");
                }
                while (script.contains((" " + script.charAt(i)))){
                    script = script.replace((" "+script.charAt(i)), (""+script.charAt(i)));
                    i--;
                }
            }
        }



        int start = 1;
        String command = "";
        //System.out.println(script.charAt(start));
        if(script.charAt(start) == '@'){
            if(script.charAt(start+1) == '.' || script.charAt(start+1) == '['){
                start++;
            }
        }
        //System.out.println(script.charAt(start));

        //if comma exists, only use the script after it is placed.
        if(script.contains(",")){ //As no commands seem to require a comma, this should be safe to do
            start = script.lastIndexOf(",");
        }

        if(script.charAt(start) == '['){ //Seems to operate correctly
            int commandStart = script.indexOf("['");
            int commandEnd = script.lastIndexOf("']");
            if (commandStart == -1 || commandEnd == -1){
                return new JsonString("NO MATCHES");
            }

            command = script.substring(start+2, commandEnd);
            //System.out.println("Coomand2 = " + command);
            start = commandEnd+2;

        } else {
            if(script.charAt(start) == '.'){
                start++;
            }

            while(!endSymbols.contains(""+script.charAt(start)) && script.charAt(start) != ')'){
                command+=script.charAt(start);
                start++;
            }
            //System.out.println("Coomand = " + command);

        }
        //System.out.println("SCRIPT = " + script);

        //Current version only allows for a single operator, and a single digit. Will expand later if there is time.
        String operation = "";
        if(script.charAt(start) != ')'){
            while(endSymbols.contains(""+script.charAt(start))){
                operation += script.charAt(start);
                start++;
            }
        }
        //System.out.println("OPERATION = " + operation);

        String endDigits = "";
        while(script.charAt(start) != ')'){
            endDigits += script.charAt(start);
            start++;
        }

        if(!endDigits.equals("")){
            try{
                Float.parseFloat(endDigits);
            } catch(NumberFormatException e){
                return new JsonString("NO MATCHES");
            }
        }

        switch(command){
            //I've only managed to find examples of script using @.length. Will add others if they exist.
            case "length":
                if(!operation.equals("") && !endDigits.equals("")){
                    int length;
                    if(search.isArray()){
                        //System.out.println("ARRAY");
                        length = ((JsonArray)search).size();
                    } else if (search.isString()){
                        //System.out.println("STRING");
                        length = search.toString().length();
                    } else {
                        //System.out.println("BAD SCRIPT");
                        return new JsonString("BAD SCRIPT");
                    }
                    float diff = Float.parseFloat(endDigits);
                    switch(operation){
                        case "+":
                            length = (int)((float)length + diff);
                            break;
                        case "-":
                            length = (int)((float)length - diff);
                            break;
                        case "*":
                            length = (int)((float)length * diff);
                            break;
                        case "/":
                            length = (int)((float)length / diff);
                            break;
                        default:
                            return new JsonString("NO MATCHES");
                    }

                    if(search.isArray()){
                        if(length >((JsonArray)search).size() || (length < 0)){
                            return new JsonString("NO MATCHES");
                        }
                        JsonElement result = ((JsonArray)search).getElement(length);
                        return result;
                    } else {
                        if(length >(search.toString().length())){
                            return new JsonString("NO MATCHES");
                        }
                        JsonString result = new JsonString("" + search.toString().charAt(length));
                        return result;
                    }

                } else {
                    return new JsonString("NO MATCHES"); //Even though it would work, length would alwasy be outside the array or string's range.
                }
            default:
                return new JsonString("NO MATCHES");
        }
    }

    private static JsonElement parseFilter(String filter, JsonElement search) {
        //System.out.println("OI ARE YOU EVEN RUNNING?");
        //System.out.println(filter);
        JsonArray matches = new JsonArray();
        JsonArray andMatches = new JsonArray();


        filter = filter.substring(3);
        //System.out.println(filter);
        ////System.exit(4);
        //System.out.println("FILER COmmand = " + filter);
        if (filter.contains(") ")) { //Can't have space after bracket
            return new JsonString("NO MATCHES");
        }

        //Remove spaces for consistency.
        while (filter.contains("( ")) {
            filter = filter.replace("( ", "(");
        }
        while (filter.contains(" )")) {
            filter = filter.replace(" )", ")");
        }

        //System.out.println("final Filter = " + filter);


        ArrayList<Number> startBrackets = new ArrayList<>();
        ArrayList<Number> endBrackets = new ArrayList<>();
        int startLocation = 0;
        int endLocation = filter.length();

        if (filter.contains("['") && (filter.contains("']"))) {
            while (filter.indexOf("['", startLocation) != -1) {
                if (endBrackets.contains(endLocation)) {
                    endLocation--;
                }
                endLocation = filter.lastIndexOf("']", endLocation);
                startLocation = filter.lastIndexOf("['", endLocation);
                if (startLocation == -1) {

                }
                startBrackets.add(startLocation);
                endBrackets.add(endLocation);
                startLocation++;
            }
        } else if (filter.contains("['") || (filter.contains("']"))) {
            return new JsonString("NO MATCHES");
        }

        startLocation = 1;
        int buffer = startLocation;
        if (filter.contains(",")) { //Everything left of comma gets ignored...
            while (filter.indexOf(",", buffer) != -1) {
                int commaPoint = filter.indexOf(",", buffer);
                boolean inBrackets = false;
                for (int i = 0; i < startBrackets.size(); i++) {
                    if (commaPoint > startBrackets.get(i).intValue() && commaPoint < endBrackets.get(i).intValue()) {
                        inBrackets = true;
                    }
                }
                if (!inBrackets) {
                    filter = filter.substring(0, commaPoint) + filter.substring(commaPoint);
                    startLocation = buffer+1;
                }
                buffer++;
            }
        }

        ////System.out.println("RESULT OF FILTER = " + filter);
        ////System.out.println("ENDING START LOCATION = " + startLocation);
        ////System.out.println("Char at start = " + filter.charAt(startLocation));


        while (startLocation < filter.length() && filter.charAt(startLocation) != ')') {
            String keyword = "";
            String function = "";
            String value = "";
            boolean notWanted = false;




            if(filter.charAt(startLocation) == '!'){
                notWanted = true;
                startLocation++;
            }

            //System.out.println("-----HERE-----");
            //System.out.println(filter.charAt(startLocation));

            if (filter.charAt(startLocation) != '@' ||
                ((filter.charAt(startLocation) == '@')
                    && ((filter.charAt(startLocation + 1) != '[')
                    && (filter.charAt(startLocation + 1) != '.')
                && (filter.charAt(startLocation + 1) != ' ')))) {
                return new JsonString("NO MATCHES");

            } else if (filter.charAt(startLocation + 1) == '[') {
                if (startBrackets.contains(startLocation + 1)) {
                    keyword = filter.substring(startLocation + 3, endLocation);
                    startLocation = endLocation+1;
                }
            } else if (filter.charAt(startLocation + 1) == '.') {
                startLocation += 2;
                while (startLocation != filter.length() && (filter.charAt(startLocation) != ' ' && filter.charAt(startLocation) != ')')) {
                    keyword += filter.charAt(startLocation);
                    startLocation++;
                    //System.out.println(keyword);
                    if(keyword.contains(".")){
                        return new JsonString("NO MATCHES");
                    }
                }
            }


            if(keyword.equals("")){
                startLocation++;
            }


            int replay = startLocation;

            if (filter.charAt(startLocation) != ')') { //Check for proper filters.
                while(filter.charAt(startLocation) == ' '){
                    startLocation++;
                }

                while (filter.charAt(startLocation) != ' ' && filter.charAt(startLocation) != ')') {
                    function += filter.charAt(startLocation);
                    startLocation++;
                }

                while(filter.charAt(startLocation) == ' '){
                    startLocation++;
                }

                if(!function.equals("&&")){
                    if(startBrackets.contains(startLocation)){
                        int index = startBrackets.indexOf(startLocation);
                        value = filter.substring(startBrackets.get(index).intValue(), endBrackets.get(index).intValue()+2);
                        startLocation = endBrackets.get(index).intValue()+1;
                    } else if (filter.charAt(startLocation) == '\''){
                        value += "'";
                        startLocation++;
                        while (filter.charAt(startLocation) != '\''&& filter.charAt(startLocation) != ')') {
                            value += filter.charAt(startLocation);
                            startLocation++;
                        }
                        if(filter.charAt(startLocation) == '\''){
                            value += "'";
                        }
                    } else {
                        while (filter.charAt(startLocation) != ' ' && filter.charAt(startLocation) != ')') {
                            value += filter.charAt(startLocation);
                            startLocation++;
                        }
                    }


                    //startLocation++;
                } else {
                    startLocation = replay;
                    function = "";
                }
            }

            //System.out.println("FUNCTION = " + function);
            //System.out.println("VALUE = " + value);

            //System.out.println("Starting element = " + search);
            //System.out.println("START LOCATION AT THIS POINT = " + startLocation);


            if(function.equals("")){
                //System.out.println("THERE IS NO FUNCTION");
                //System.out.println("search = " + search);
                if(search.isObject()){
                    Set<String> keySet = ((JsonObject)search).getKeys();
                    for(String key: keySet){
                        if (((JsonObject)search).getProperty(key).isNull()){
                            //System.out.println("NOW THE ERROR SHOWS ITSELF");
                            return new JsonString("NullFail");
                        }
                    }
                    for(String key: keySet){
                        JsonElement result = ((JsonObject)search).getProperty(key);
                        if(result.isObject()){
                            Set<String> innerKeySet = ((JsonObject)result).getKeys();
                            if(innerKeySet.contains(keyword) != notWanted){
                                //System.out.println("F1: Result = " + result);
                                matches.addElement(result);
                            }
                        }
                    }

                } else if (search.isArray()) {
                    for(int x = 0; x < ((JsonArray)search).size(); x++){
                        JsonElement result = ((JsonArray)search).getElement(x);
                        if(result.isObject()){
                            Set<String> keySet = ((JsonObject)result).getKeys();
                            if(keySet.contains(keyword) != notWanted){
                                //System.out.println("F2: Result = " + result);
                                matches.addElement(result);
                            }
                        }
                    }
                } else {
                    return new JsonString("NO MATCHES");
                }
                //System.out.println("AT THE END HERE!!!");
            } else if(keyword.equals("")){ //Assume case of (@ == something) etc.
                //System.out.println("THERE IS NO KEYWORD");
                if(search.isObject()){
                    Set<String> keySet = ((JsonObject)search).getKeys();
                    for(String key: keySet){
                        if(parseFilterInput(function, value, ((JsonObject)search).getProperty(key))){
                            //System.out.println("parseFilter 1 Success");
                            matches.addElement(((JsonObject)search).getProperty(key));
                            //System.out.println("APPENDED MATCHES = " + matches);
                        }
                    }
                } else if (search.isArray()){
                    int arraySize = ((JsonArray) search).size();
                    for (int i = 0; i < arraySize; i++) {
                        if(parseFilterInput(function, value, ((JsonArray)search).getElement(i))){
                            //System.out.println("parseFilter 2 Success");
                            matches.addElement(((JsonArray)search).getElement(i));
                        }
                    }
                } else {
                    return new JsonString("NO MATCHES");
                }
            } else {
                if(value.equals("")){
                    return new JsonString("NO MATCHES");
                }
                if (search.isObject()) {
                    Set<String> keys = ((JsonObject) search).getKeys();
                    for(String key: keys){
                        if (((JsonObject)search).getProperty(key).isNull()){
                            //System.out.println("NOW THE ERROR SHOWS ITSELF");
                            return new JsonString("NullFail");
                        }
                    }
                    for (String key : keys) {
                        JsonElement result = ((JsonObject) search).getProperty(key);
                        if (result.isObject()) {
                            Set<String> innerKeys = ((JsonObject) result).getKeys();
                            if(function.equals("!=") && !innerKeys.contains(keyword)){
                                matches.addElement(result);
                            } else if (innerKeys.contains(keyword) != notWanted) {
                                if (parseFilterInput(function, value, ((JsonObject) result).getProperty(keyword))) {
                                    //System.out.println("parseFilter 3 Success");
                                    matches.addElement(result);
                                    //System.out.println("MATCHES HERE = " + matches);
                                }
                            }
                        } else if (function.equals("!=")){
                            if(result.isNull()){
                                //System.out.println("F4: Result = " + result);
                                return new JsonString("NullFail");
                            } else {
                                matches.addElement(result);
                            }
                        }
                    }
                } else if (search.isArray()) {
                    int arraySize = ((JsonArray) search).size();
                    for (int i = 0; i < arraySize; i++) {
                        JsonElement result = ((JsonArray) search).getElement(i);
                        if (result.isObject()) {
                            Set<String> innerKeys = ((JsonObject) result).getKeys();
                            if(function.equals("!=") && !innerKeys.contains(keyword)){
                                matches.addElement(result);
                            } else if (innerKeys.contains(keyword) != notWanted) {
                                if (parseFilterInput(function, value, ((JsonObject) result).getProperty(keyword))) {
                                    //System.out.println("parseFilter 4 Success");
                                    //System.out.println("F5: Result = " + result);
                                    if(result.isNull()){
                                        return new JsonString("NullFail");
                                    } else {
                                        matches.addElement(result);
                                    }
                                }
                            }
                        } else if (function.equals("!=")){
                            //System.out.println("F6: Result = " + result);
                            if(result.isNull()){
                                return new JsonString("NullFail");
                            } else {
                                matches.addElement(result);
                            }
                        }

                    }
                }
            }
            if(filter.charAt(startLocation) == '\'' || filter.charAt(startLocation) == ']'){ //One fixes string inputs, other fixes bracket inputs.
                startLocation++;
            }

            //System.out.println(filter.charAt(startLocation));
            if(filter.charAt(startLocation) != ')'){
                //System.out.println("WHERE DID YOU WANT TO GO?");
                startLocation++;
                String andCheck = "";
                //System.out.println(startLocation);
                while(startLocation != filter.length()-1 && filter.charAt(startLocation) != ' ' && filter.charAt(startLocation) != ')'){
                    andCheck += filter.charAt(startLocation);
                    startLocation++;
                }
                //System.out.println(andCheck);
                if(andCheck.equals("&&")){
                    andMatches = matches;
                    matches = new JsonArray();
                    startLocation++;
                } else {
                    return new JsonString("NO MATCHES");
                }
            } else if (andMatches.size() != 0) {
                JsonArray bufferMatches = matches;
                matches = new JsonArray();
                for(int i = 0; i < bufferMatches.size(); i++){
                    for(int j = 0; j < andMatches.size(); j++){
                        if(bufferMatches.getElement(i) == andMatches.getElement(j)){
                            matches.addElement(bufferMatches.getElement(i));
                        }
                    }
                }
                //System.out.println("FINAL RESULT OF AND!!!");
                //System.out.println(matches);

                if(filter.charAt(startLocation) != ')'){
                    andMatches = matches;
                    matches = new JsonArray();
                }
            }
        }
        return matches;
    }

    private static boolean parseFilterInput(String operation, String value, JsonElement element){
        //System.out.println("You made it to the filter input!");
        if(element == null){
            //Should only occur if user was trying to use negate (!@) with operators and values.
            return false;
        }
        String jsonValue = element.toString();
        //System.out.println("Saved Value = " + value);
        //System.out.println("FilterInputElement = " + element);
        boolean validString = (value.indexOf("'") == 0 && value.lastIndexOf("'") == value.length()-1);

        switch(operation){
            case "==":
                //System.out.println(validString);
                if(element.isString()){
                    if(validString){
                        value = value.substring(1, value.length()-1);
                    } else {
                        return false;
                    }
                }
                if(jsonValue.equals(value)){
                    //System.out.println("WE GOT A MATCH HERE!!!");
                    return true;
                }
                break;
            case "!=":
                if(element.isString()){
                    if(validString){
                        value = value.substring(1, value.length()-1);
                    } else {
                        return false;
                    }
                }
                if(!jsonValue.equals(value)){
                    return true;
                }
                break;
            case "<":
                if(element.isNumber()){
                    try{
                        float valueWanted = Float.parseFloat(value);
                        float jsonNumeral = Float.parseFloat(((JsonNumber)element).toString());
                        if(jsonNumeral < valueWanted){
                            return true;
                        }
                    } catch (NumberFormatException e){
                        return false;
                    }
                } else {
                    return false;
                }
                break;
            case "<=":
                if(element.isNumber()){
                    try{
                        float valueWanted = Float.parseFloat(value);
                        float jsonNumeral = Float.parseFloat(((JsonNumber)element).toString());
                        if(jsonNumeral <= valueWanted){
                            return true;
                        }
                    } catch (NumberFormatException e){
                        return false;
                    }
                } else {
                    return false;
                }
                break;
            case ">":
                if(element.isNumber()){
                    try{
                        float valueWanted = Float.parseFloat(value);
                        float jsonNumeral = Float.parseFloat(((JsonNumber)element).toString());
                        if(jsonNumeral > valueWanted){
                            return true;
                        }
                    } catch (NumberFormatException e){
                        return false;
                    }
                } else {
                    return false;
                }
                break;
            case ">=":
                if(element.isNumber()){
                    try{
                        float valueWanted = Float.parseFloat(value);
                        float jsonNumeral = Float.parseFloat(((JsonNumber)element).toString());
                        if(jsonNumeral >= valueWanted){
                            return true;
                        }
                    } catch (NumberFormatException e){
                        return false;
                    }
                } else {
                    return false;
                }
                break;
            case "=~":
                if(validString){
                    value = value.substring(1, value.length()-1);
                }
                //System.out.println("JSONVALUE = " + jsonValue);
                //System.out.println("VALUE = " + value);
                if(jsonValue.matches(value)){
                    //System.out.println("WE GOT A MATCHING REGEX");
                    return true;
                } else {
                    return false;
                }

            case "in":
                if(value.charAt(0) == '[' && value.charAt(value.length()-1) == ']'){
                    value = value.substring(1, value.length()-1);
                    value = value.replace(", ", ",");
                    String[] valueSplit = value.split(",");
                    //System.out.println("RESULT OF IN = " + Arrays.toString(valueSplit));
                    //System.out.println("'" + jsonValue + "'");
                    for (String s : valueSplit) {
                        if (("'" + jsonValue + "'").equals(s)) {
                            //System.out.println("MATCH FOUND!!!");
                            return true;
                        }
                    }
                }
                return false;
            case "nin":
                if(value.charAt(0) == '[' && value.charAt(value.length()-1) == ']'){
                    value = value.substring(1, value.length()-1);
                    value = value.replace(", ", ",");
                    String[] valueSplit = value.split(",");
                    //System.out.println("RESULT OF IN = " + Arrays.toString(valueSplit));
                    //System.out.println("'" + jsonValue + "'");
                    for (String s : valueSplit) {
                        if (("'" + jsonValue + "'").equals(s)) {
                            //System.out.println("MATCH FOUND!!!");
                            return false;
                        }
                    }
                }
                return true;
            case "size":
                if(element.isString()){
                    try{
                        int size = Integer.parseInt(value);
                        if (jsonValue.length() == size){
                            return true;
                        }
                    } catch (NumberFormatException e){
                        return false;
                    }
                } else if(element.isArray()){
                    try{
                        int size = Integer.parseInt(value);
                        if (((JsonArray)element).size() == size){
                            return true;
                        }
                    } catch (NumberFormatException e){
                        return false;
                    }
                } else {
                    return false;
                }
                break;
            case "contains":
                if(element.isString()){
                    if(validString){
                        value = value.substring(1, value.length()-1);
                    }
                    //System.out.println("The value was: " + value);
                    return jsonValue.contains(value);
                } else if (element.isArray()) {
                    try {
                        int size = Integer.parseInt(value);
                        if (((JsonArray)element).size() >= size){ //Should it be >= or >
                            return true;
                        }
                    } catch (NumberFormatException e){
                        return false;
                    }
                }
                return false;

            case "empty":
                if(value.equals("true")){
                    if(element.isObject()){
                        return ((JsonObject)element).getKeys().size() == 0;
                    } else if (element.isArray()){
                        return ((JsonArray)element).size() == 0;
                    } else {
                        return element.isNull();
                    }
                } else if (value.equals("false")){
                    if(element.isObject()){
                        return ((JsonObject)element).getKeys().size() != 0;
                    } else if (element.isArray()){
                        return ((JsonArray)element).size() != 0;
                    } else {
                        return !element.isNull();
                    }
                } else {
                    return false;
                }
            default:
                return false;
        }
        return false;
    }

}
