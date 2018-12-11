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
import com.modak.utils.JSONUtils;
import com.modak.utils.MessageUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * The FactoryBot, while itself technically a Bot is a part of the Bot ecosystem bootstrapping process.  Typically you
 * will have one of these objects on a given host and it has the reponsibility of processing the messages to create and
 * launch the additional Bots being used by the system.
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-08.
 */
public class FactoryBot extends BaseBot {

    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

    private ArrayList<HashMap<String, Object>> botsConfig = null;


    /**
     * Starting of FactoryBot.
     *
     * @param config configuration to start bot factory.
     * @throws Exception when unable to start bot factory.
     */
    @Override
    public void initBot(HashMap<String, Object> config) throws Exception {

        if (config != null ) {
        super.initBot(config);
        }
        else {
            throw new BotException(BotCommon.INVALID_INPUT_CONFIGURATIONS);
        }
    }

    /**
     * Starts all the bots which are in datamap.
     *
     * @param dataMap           contains list of bots to start.
     * @param appTemplateOutput rendered output from template.
     * @return string with the status.
     * @throws Exception when unable to start a bot.
     */
    @Override
    protected String processBotLogic(HashMap<String, Object> dataMap, String appTemplateOutput) throws Exception {
        logger.debug(BotCommon.DATAMAP + new JSONObject(dataMap));
        try {
            botsConfig = MessageUtils
                    .getArrayList(dataMap, BotCommon.BOTS_LIST);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BotException(e.getMessage());
        }

        if (botsConfig != null && botsConfig.size() > 0 ) {
            for (HashMap<String, Object> botConfig : botsConfig) {
                try {
                    int instances = MessageUtils.getInteger(botConfig, BotCommon.NO_OF_INSTANCES);
                    String botConfigPath = MessageUtils.getString(botConfig, BotCommon.FACTORY_BOT_CONFIG);
                    for (int i = 0; i < instances; i++) {
                        try {
                            Runnable startBot = () -> {
                                HashMap<String, Object> configMap = null;
                                String botClassName = null;
                                try {

                                    String botConfigJSON = FileUtils
                                            .readFileToString(
                                                    new File(botConfigPath),
                                                    Charset.defaultCharset());
                                    configMap = JSONUtils.jsonToMap(botConfigJSON);
                                    botClassName = MessageUtils.getString(configMap, BotCommon.BOT_CLASSNAME);
                                    Bot bot = (Bot) Class.forName(botClassName).newInstance();
                                    bot.initBot(configMap);
                                    bot.startBot();
                                    logger.debug(BotCommon.STARTED_A_NEW_BOT + " : " + botClassName);
                                }
                                catch (Exception ex) {
                                    ex.printStackTrace();
                                    logger.error(BotCommon.FAILED_TO_START_BOT, ex);
                                }
                            };

                            new Thread(startBot).start();

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            logger.error(BotCommon.FAILED_START_BOT_CONFIG + botConfig, e);
                            throw new BotException(e.getMessage());
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    logger.error(BotCommon.EXCEPTION_OCCURED_STARTING_BOT, e);
                    throw new BotException(BotCommon.EXCEPTION_OCCURED_STARTING_BOT + " : " + e.getMessage());
                }
            }
        }
        else {
            logger.error(BotCommon.BOTSCONFIG_IS_NULL_OR_PROCESSID);
        }
        return null;
    }

    /**
     * Shutdowns the connection manager
     *
     * @throws Exception fails to shutdown connection manager
     */
    @Override
    public void shutdown() throws Exception {
        super.shutdown();
    }
}
