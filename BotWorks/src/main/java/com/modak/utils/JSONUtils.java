/*
   Copyright 2018 modakanalytics.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.modak.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import scala.util.parsing.json.JSON;


/**
 * This class does the JSON conversions
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-02
 */
public class JSONUtils {
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

    /**
     * Checks to see if a String can be parsed into a JSONObject.
     *
     * @param json - The String to parse.
     * @return true if there is no exception generated when the string is parsed.
     */
    public static boolean isJSON(String json) {
        boolean retval = true;
        try {
            JSONObject jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            retval = false;
        }
        return retval;
    }

    /**
     * Converts the json string to map
     *
     * @param json The String to parse
     * @return returns the hashMap after converting it
     * @throws JSONException if the json string has syntax errors
     */

    public static HashMap<String, Object> jsonToMap(String json) throws JSONException {
        HashMap<String, Object> retMap = new HashMap<String, Object>();
        if (json != null) {
            JSONObject jsonObject = new JSONObject(json);
            retMap = toMap(jsonObject);
        }
        return retMap;
    }

    /**
     * Converts the jsonobject string to map
     *
     * @param jsonObject The String to parse
     * @return returns the hashMap after converting it
     * @throws JSONException if the json string has syntax errors
     */

    public static HashMap<String, Object> jsonToMap(JSONObject jsonObject) throws JSONException {
        HashMap<String, Object> retMap = new HashMap<String, Object>();
        if (jsonObject != null) {
            retMap = toMap(jsonObject);
        }
        return retMap;
    }

    /**
     * Converts the JsonObject to a map
     *
     * @param object the input required to convert it into a map
     * @return returns the hashMap
     * @throws JSONException if the Object sent has syntax errors
     */
    public static HashMap<String, Object> toMap(JSONObject object) throws JSONException {
        HashMap map = new HashMap();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }

            if (value.equals(JSONObject.NULL)) {
                map.put(key, null);
            } else {
                map.put(key, value);
            }

        }
        return map;
    }

    /**
     * Converts JSONArray to a list
     *
     * @param array the input array to be passed to convert it to a list
     * @return returns array list
     * @throws JSONException if the array is null
     */
    public static ArrayList<Object> toList(JSONArray array) throws JSONException {
        ArrayList<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);

        }
        return list;
    }


    /**
     * Convert an object tree consisting of Map, List, and primitive types to a JSON string.
     *
     * @param object "Data Structure" in the form of nested Map, List, and primitive types
     * @return JSON String
     */
    public static String object2JsonString(Object object) {
        return object2JsonString(object, 0, 0);
    }

    public static String object2JsonString(Object object, int indentSize, int indentLevel) {
        if (object instanceof String) {
            return "\"" + quoteQuote(object) + "\"";
        } else if (object instanceof Map<?, ?>) {
            return map2JsonString((Map<String, Object>) object, indentSize, indentLevel);
        } else if (object instanceof List<?>) {
            return list2JsonString((List<Object>) object, indentSize, indentLevel);
        } else {
            return object == null ? "null" : object.toString();
        }
    }

    private static String quoteQuote(Object object) {
        return object.toString().replaceAll("\"", "\\\\\"");
    }

    /**
     * Convert a map to a JSON string.
     *
     * @param map Map of Map, List, or primitive types
     * @return String representation of map as JSON
     */
    public static String map2JsonString(Map<String, Object> map) {
        return map2JsonString(map, 0);
    }

    public static String map2JsonString(Map<String, Object> map, int indentSize) {
        return map2JsonString(map, indentSize, 0);
    }

    /**
     * Convert a map to a JSON string.
     *
     * @param map        Map of Map, List, or primitive types
     * @param indentSize size of indent to include in the output string, 0 implies no indentation
     * @return String representation of map as JSON
     */
    public static String map2JsonString(Map<String, Object> map, int indentSize, int indentLevel) {
        StringBuilder string = new StringBuilder();
        if (map.size() == 0) {
            string.append("{}");
        } else {
            for (String key : map.keySet()) {
                if (string.length() == 0) {
                    string.append(indentSize == 0 ? "{ " : "{\n");
                    if (indentSize > 0) {
                        ++indentLevel;
                        indent(string, indentSize, indentLevel);
                    }
                } else {
                    string.append(indentSize == 0 ? ", " : ",\n");
                    indent(string, indentSize, indentLevel);
                }
                string.append(object2JsonString(key)).append(" : ")
                        .append(object2JsonString(map.get(key), indentSize, indentLevel));
            }
            string.append(indentSize == 0 ? " }" : "\n");
            if (indentSize > 0) {
                --indentLevel;
                indent(string, indentSize, indentLevel);
                string.append("}");
            }
        }
        return string.toString();
    }

    private static void indent(StringBuilder string, int indentSize, int indentLevel) {
        for (int j = 0; j < indentLevel; j++) {
            for (int i = 0; i < indentSize; i++) {
                string.append(" ");
            }
        }
    }


    public static String list2JsonString(List<Object> list, int indentSize, int indentLevel) {
        StringBuilder string = new StringBuilder();
        if (list.size() == 0) {
            string.append("[]");
        } else {
            for (Object value : list) {
                if (string.length() == 0) {
                    string.append(indentSize == 0 ? "[ " : "[\n");
                    if (indentSize > 0) {
                        ++indentLevel;
                        indent(string, indentSize, indentLevel);
                    }
                } else {
                    string.append(indentSize == 0 ? ", " : ",\n");
                    indent(string, indentSize, indentLevel);
                }
                string.append(object2JsonString(value, indentSize, indentLevel));
            }
            string.append(indentSize == 0 ? " ]" : "\n");
            if (indentSize > 0) {
                --indentLevel;
                indent(string, indentSize, indentLevel);
                string.append("]");
            }
        }
        return string.toString();
    }

    public static Object jsonStringToObject(String jsonString) {

        Object obj = convertJSONObjToString(jsonString);
        if (obj == null) {
            obj = convertJSONArrayObjToString(jsonString);
            if (obj == null) {
                obj = jsonString;
            }
        }
        return obj;
    }

    public static Object convertJSONObjToString(String jsonString) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
        }
        return jsonObject != null ? jsonObject.toMap() : null;
    }

    public static Object convertJSONArrayObjToString(String jsonString) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
        }
        return jsonArray != null ? jsonArray.toList() : null;
    }

}
