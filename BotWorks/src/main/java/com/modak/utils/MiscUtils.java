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


import java.util.HashMap;
import org.apache.logging.log4j.LogManager;


/**
 * This class does the encoding,decoding and formatting the string like replacing \r \n \t with space
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-02
 */
public class MiscUtils {
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
    public static String getProperty(HashMap<String, Object> map, String property) throws Exception {

        if (property != null && map != null && map.get(property) != null) {
            return map.get(property).toString();
        } else {
            throw new Exception("Property not found.");
        }
    }

    public static String getProperty(HashMap<String, Object> map, String property, String defaultValue) throws Exception {

        if (property != null && map != null && map.get(property) != null) {
            return map.get(property).toString();
        } else if (defaultValue != null) {
            return defaultValue;
        } else {
            throw new Exception("Propery not found");
        }
    }

    /**
     * Allows stripping new line characters from a string so that it can be used for logging purposes.
     *
     * @param input           - The String to optionally strip of
     * @param remove_newlines - If true, new lines will be removed from the string and replace with a single space.
     * @return The original string or a string that has newlines removed.
     */
    public static String formatString(String input, boolean remove_newlines) {
        String output = input;

        if (remove_newlines && input != null) {
            String foo = new String(input).replaceAll("[\n\r\t]", " ");
            output = new String(foo).replaceAll("( )+", " ");
        }
        return output;
    }
}
