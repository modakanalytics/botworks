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


import com.modak.botworks.bots.BaseBot;
import com.modak.botworks.bots.BotException;
import com.modak.botworks.bots.common.BotCommon;
import com.modak.utils.MiscUtils;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;

/**
 * This class starts LogMsgBot with the supplied properties
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-15
 */
public class LogMsgBot extends BaseBot {

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

    /**
     * this is used to initialize log message bot
     *
     * @param configuration this HashMap contains the necessary information for initializing log msg bot implementation
     * @throws Exception throws when bots is not initialized
     */
    @Override
    public void initBot(HashMap<String, Object> configuration) throws Exception {
        super.initBot(configuration);
        if (configuration != null) {
            super.initBot(configuration);
        }
        else {
            logger.error(BotCommon.INVALID_ARG);
            throw new BotException(BotCommon.INVALID_ARG);
        }

    }

    /**
     * @param dataMap data to be processed.
     * @param appTemplateOutput on which the dataMap is applied.
     * @return nothing.
     * @throws Exception when unable to process dataMap.
     */
    protected String processBotLogic(HashMap<String, Object> dataMap, String appTemplateOutput) throws Exception {
        return null;
    }

    /**
     * @param message to be logged.
     */
    @Override
    public void processMessage(String message) {

        if (message != null) {
            logger.info("Message Received in LogMsgBot : " + MiscUtils.formatString(message, true));
        }
    }

}
