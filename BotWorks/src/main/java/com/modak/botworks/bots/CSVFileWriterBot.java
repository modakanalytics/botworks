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


package com.modak.botworks.bots;

import com.modak.botworks.bots.common.BotCommon;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * This CSVFileWriterBot creates a file local to the bot being run with the supplied file content
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-02
 */

public class CSVFileWriterBot extends BaseBot {

    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

    /**
     * This is used to init CSVFileWriterBot which is used to write data to a csv file
     *
     * @param configuration This HashMap contains the necessary information for initializing the  bot
     * @throws Exception If the Bot cannot be initialized.
     */
    @Override
    public void initBot(HashMap<String, Object> configuration) throws Exception {
        if (configuration != null) {
            super.initBot(configuration);
        } else {
            throw new BotException(BotCommon.INVALID_INPUT_CONFIGURATIONS);
        }
    }

    /**
     * This method executes for every incoming bot message and creates a new file local to the bot being run
     *
     * @param dataMap           This should be a HashMap which hold the data required to process the Bot logic
     * @param appTemplateOutput The String template output obtained by applying the data to appTemplate
     * @return returns the success of failure string for the processed bot logic of CSVFileWriterBot
     * @throws Exception
     */
    @Override
    protected String processBotLogic(HashMap<String, Object> dataMap, String appTemplateOutput) throws Exception {
        HashMap map = new HashMap();
        try {
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataMap.get(BotCommon.DATA_LIST);
            List<Map<String, Object>> processedList = new ArrayList<>();
            String fileName = dataList.get(0).get(BotCommon.TABLE_NAME).toString();
            dataList.forEach(i -> {
                i.remove(BotCommon.TABLE_NAME);
                processedList.add(i);
            });
            File file = new File(fileName);
            String csvString = generateCSVFromList(processedList);
            FileUtils.write(file, csvString, Charset.defaultCharset());
            map.put(BotCommon.STATUS, BotCommon.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            map.put(BotCommon.STATUS, BotCommon.FAILED);
        }
        return new JSONObject(map).toString();
    }


    /**
     * This method converts a List of Map objects to CSV String format
     *
     * @param list - This is a list of map objects
     * @return - returns a CSV String format for the supplied list of map object
     */
    private static String generateCSVFromList(List<Map<String, Object>> list) throws Exception {
        final StringBuffer csvString = new StringBuffer();
        if (list.size() > 0) {
            List<String> headers = list.stream().flatMap(map -> map.keySet().stream()).distinct().collect(toList());
            for (int i = 0; i < headers.size(); i++) {
                csvString.append(headers.get(i));
                csvString.append(i == headers.size() - 1 ? "\n" : ",");
            }
            for (Map<String, Object> map : list) {
                for (int i = 0; i < headers.size(); i++) {
                    csvString.append(map.get(headers.get(i)));
                    csvString.append(i == headers.size() - 1 ? "\n" : ",");
                }
            }
        }
        return csvString.toString();
    }


}
