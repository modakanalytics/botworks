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

import com.modak.botworks.bots.common.BotCommon;
import com.modak.botworks.bots.processor.MessageProcessor;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * This class has the utility methods to get the values for the keys present in the Bot ecosystem JSON messages. Given a
 * key and a Map this class gives utilitiy methods which gets values for the entries from the Bot's JSON message
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-09-20
 */

public class MessageUtils {

    private static final org.apache.logging.log4j.Logger logger = LogManager
        .getLogger(MessageUtils.class.getSimpleName());

    /**
     * The key can be a multilevel key e.g. "metadata.msg_uuid".
     *
     * @return the HashMap based on a key. Null if the element is not found or is not of the type HashMap
     */
    public static HashMap<String, Object> getMap(HashMap<String, Object> sourceMap, String key) {
        if (sourceMap == null || key == null) {
            return null;
        }
        String[] parts = key.split("\\.");
        int i = 0;
        while (i < parts.length - 1) {
            sourceMap = getMap(sourceMap, parts[i]);
            i++;
        }
        if (sourceMap.get(parts[i]) != null) {
            if (sourceMap.get(parts[i]) instanceof HashMap) {
                return (HashMap<String, Object>) sourceMap.get(parts[i]);
            }
        }
        return null;
    }

    /**
     * The key can be a multilevel key e.g. "metadata.tags".
     *
     * @return the ArrayList based on a key. Null if the element is not found or is not of the type ArrayList
     */
    public static ArrayList getArrayList(HashMap<String, Object> sourceMap, String key) {
        if (sourceMap == null || key == null) {
            return null;
        }
        String[] parts = key.split("\\.");
        int i = 0;
        while (i < parts.length - 1) {
            sourceMap = getMap(sourceMap, parts[i]);
            i++;
        }
        if (sourceMap.get(parts[i]) != null) {
            if (sourceMap.get(parts[i]) instanceof ArrayList) {
                return (ArrayList) sourceMap.get(parts[i]);
            }
        }
        return null;
    }

    /**
     * The key can be a multilevel key e.g. "metadata.uuid".
     *
     * @return the String based on a key. Null if the element is not found or is not of the type String
     */
    public static String getString(HashMap<String, Object> sourceMap, String key) {
        if (sourceMap == null || key == null) {
            return null;
        }
        String[] parts = key.split("\\.");
        int i = 0;
        while (i < parts.length - 1) {
            sourceMap = getMap(sourceMap, parts[i]);
            i++;
        }
        if (sourceMap.get(parts[i]) != null) {
            if (sourceMap.get(parts[i]) instanceof String) {
                return (String) sourceMap.get(parts[i]);
            }
        }
        return null;
    }

    /**
     * The key can be a multilevel key e.g. "metadata.uuid".
     *
     * @return the String based on a key. Null if the element is not found or is not of the type Integer
     */
    public static Integer getInteger(HashMap<String, Object> sourceMap, String key) {
        if (sourceMap == null || key == null) {
            return null;
        }
        String[] parts = key.split("\\.");
        int i = 0;
        while (i < parts.length - 1) {
            sourceMap = getMap(sourceMap, parts[i]);
            i++;
        }
        if (sourceMap.get(parts[i]) != null) {
            if (sourceMap.get(parts[i]) instanceof Integer) {
                return (Integer) sourceMap.get(parts[i]);
            }
        }
        return null;
    }

    /**
     * The key can be a multilevel key e.g. "metadata.uuid".
     *
     * @return the String based on a key. Null if the element is not found or is not of the type Long
     */
    public static Long getLong(HashMap<String, Object> sourceMap, String key) {
        if (sourceMap == null || key == null) {
            return null;
        }
        String[] parts = key.split("\\.");
        int i = 0;
        while (i < parts.length - 1) {
            sourceMap = getMap(sourceMap, parts[i]);
            i++;
        }
        if (sourceMap.get(parts[i]) != null) {
            if (sourceMap.get(parts[i]) instanceof Long) {
                return (Long) sourceMap.get(parts[i]);
            }
        }
        return null;
    }

    /**
     * The key can be a multilevel key e.g. "metadata.uuid".
     *
     * @return the String based on a key. Null if the element is not found or is not of the type Boolean
     */
    public static Boolean getBoolean(HashMap<String, Object> sourceMap, String key) {
        if (sourceMap == null || key == null) {
            return null;
        }
        String[] parts = key.split("\\.");
        int i = 0;
        while (i < parts.length - 1) {
            sourceMap = getMap(sourceMap, parts[i]);
            i++;
        }
        if (sourceMap.get(parts[i]) != null) {
            if (sourceMap.get(parts[i]) instanceof Boolean) {
                return (Boolean) sourceMap.get(parts[i]);
            }
        }
        return null;
    }

    /**
     * The key can be a multilevel key e.g. "metadata.uuid".
     *
     * @return the Object based on a key. Null if the element is not found
     */
    public static Object get(HashMap<String, Object> sourceMap, String key) {
        if (sourceMap == null || key == null) {
            return null;
        }
        String[] parts = key.split("\\.");
        int i = 0;
        while (i < parts.length - 1) {
            sourceMap = getMap(sourceMap, parts[i]);
            i++;
        }
        if (sourceMap.get(parts[i]) != null) {
            return sourceMap.get(parts[i]);
        }
        return null;
    }

    /**
     * The key can be a multilevel key e.g. "metadata.uuid".
     *
     * @return the Object based on a key. Null if the element is not found
     */
    public static void put(HashMap<String, Object> sourceMap, String key, Object value) throws Exception {
        if (sourceMap == null) {
            throw new NullPointerException("SourceMap argument is null");
        }
        if (key == null) {
            throw new NullPointerException("Key argument is null");
        }
        String[] parts = key.split("\\.");
        int i = 0;
        while (i < parts.length - 1) {
            sourceMap = getMap(sourceMap, parts[i]);
            i++;
        }
        if (sourceMap != null) {
            sourceMap.put(parts[i], value);
        }
        else {
            throw new Exception("Parent maps not found");
        }
    }

    /**
     * Utility method for getting msg_uuid.
     *
     * @return the UUID of message
     */
    public static String getUUID(HashMap<String, Object> sourceMap) {
        return getString(sourceMap, "metadata.msg_uuid");
    }

    /**
     * Utility method for getting topic.
     *
     * @return the topic to be published
     */
    public static String getTopic(HashMap<String, Object> sourceMap) {
        return getString(sourceMap, "metadata.topic");
    }

    /**
     * Utility method for getting parent_uuid.
     *
     * @return the parent UUID of message
     */
    public static String getParentUUID(HashMap<String, Object> sourceMap) {
        return getString(sourceMap, "metadata.parent_uuid");
    }

    /**
     * Utility method for getting Tasks.
     *
     * @return Array of Task(s)
     */
    public static ArrayList getTasks(HashMap<String, Object> sourceMap) {
        return getArrayList(sourceMap, "tasks");
    }

    /**
     * Utility method for getting  processor of Task.
     *
     * @return task processor
     */
    public static HashMap<String, Object> getProcessor(HashMap<String, Object> sourceMap) {
        return getMap(sourceMap, "processor");
    }

    /**
     * Utility method for getting Data list of a task.
     *
     * @return data list of a task
     */
    public static HashMap<String, Object> getTaskData(HashMap<String, Object> sourceMap) {
        return getMap(sourceMap, "data");
    }

    /**
     * Utility method for getting tags.
     *
     * @return Array of Tag(s)
     */
    public static HashMap getTags(HashMap<String, Object> sourceMap) {
        return getMap(sourceMap, "metadata.tags");
    }

    /**
     * Utility to render string template message
     *
     * @param templateGroupName StringTemplate group name
     * @param templateName StringTemplate template name
     * @param outputData data to be applied StringTemplate
     * @param inputMessageMap input bot message
     * @param taskmap Hashmap of task details
     * @param botUUID uuid  bot generating the message
     * @param taskStartTimestamp start timestamp of the task
     * @return rendered template message as string
     */
    public static String renderMessage(String templateGroupName, String templateName,
        String outputData, Object dataObj, HashMap<String, Object> inputMessageMap, HashMap<String, Object> taskmap,
        String botUUID, long taskStartTimestamp) {
        HashMap<String, Object> templateInputs = new HashMap<>();
        templateInputs.put(BotCommon.DATA, outputData);
        templateInputs.put(BotCommon.DATA_OBJ, dataObj);
        if (inputMessageMap != null) {
            templateInputs.put(BotCommon.PARENT_METADATA, MessageUtils.getMap(inputMessageMap, BotCommon.METADATA));
            templateInputs.put(BotCommon.PARENT_BOT_TYPE, MessageUtils.getString(inputMessageMap, BotCommon.BOT_TYPE));
            templateInputs.put(BotCommon.PARENT_TASK_NAME, MessageUtils.getString(taskmap, BotCommon.TASK_NAME));
        }
        templateInputs.put(BotCommon.MESSAGE_UUID, UUID.randomUUID().toString());
        templateInputs.put(BotCommon.BOT_UUID, botUUID);
        templateInputs.put(BotCommon.PARENT_TASK_START_TS, taskStartTimestamp);
        templateInputs.put(BotCommon.PARENT_TASK_END_TS, System.currentTimeMillis());
        String outputMessage = StringTemplateUtils.renderTemplate(templateGroupName, templateName, templateInputs);
        return outputMessage;
    }

    /**
     * this method processes the dataMap
     */
    public static HashMap<String, Object> processData(HashMap<String, Object> dataMap,
        HashMap<String, Object> processorInputMap) throws Exception {

        if (dataMap == null) {
            throw new NullPointerException("DataMap parameter is null");
        }
        if (processorInputMap != null) {
            String classname = MessageUtils.getString(processorInputMap, "classname");
            if (classname != null) {
                HashMap<String, Object> props = MessageUtils.getMap(processorInputMap, "props");
                MessageProcessor processor = (MessageProcessor) Class.forName(classname).newInstance();
                HashMap<String, Object> processDataMapOutput = processor.process(dataMap, props);
                return processDataMapOutput;
            }
        }
        return dataMap;
    }

    /**
     * This message returns true or false based on the message well formed output, true if formed well else false
     *
     * @param message the message for which the wellformed check is performed
     * @return boolean true if well formed else false
     */
    public static boolean isMessageWellFormed(String message) {
        if (message == null) {
            logger.debug(MessageUtils.class.getCanonicalName() + " : Message is null");
            return false;
        }

        if (!JSONUtils.isJSON(message)) {
            logger.debug(MessageUtils.class.getCanonicalName() + " : Message is malformed JSON");
            return false;
        }

        HashMap map = JSONUtils.jsonToMap(message);
        if (map == null) {
            logger.debug(MessageUtils.class.getCanonicalName() + " : Message is malformed JSON");
            return false;
        }

        Object tasksObj = map.get("tasks");
        if (!(tasksObj != null && tasksObj instanceof ArrayList)) {
            logger.debug(MessageUtils.class.getCanonicalName() + " : Bot Message doesn't have Tasks");
            return false;
        }

        Object metadataObj = map.get("metadata");
        if (!(metadataObj != null && metadataObj instanceof HashMap)) {
            logger.debug(MessageUtils.class.getCanonicalName() + " : Bot Message doesn't have Metadata");
            return false;
        }

        return true;
    }

    /**
     * Utility method for getting parent_uuid.
     *
     * @return the parent UUID of message
     */
    public static boolean getTaskDependency(HashMap<String, Object> sourceMap) {
        return isTrue(getBoolean(sourceMap, "metadata.tags.task_dependency"));
    }

    /**
     * Checks if a Boolean value is true, handling null by returning true.
     */
    public static boolean isTrue(Boolean booleanValue) {
        return booleanValue != null ? booleanValue : false;
    }

}
