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

package com.modak.botworks;

import com.modak.botworks.bots.Bot;
import com.modak.botworks.bots.common.BotCommon;
import com.modak.utils.JSONUtils;
import com.modak.utils.SpawnBotsUtility;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * BotWorks is the main class of the BotWorks ecosystem.  It initializes class that monitors and logs
 * all messages flowing through the ecosystem as well as instantiating a FactoryBot class which will
 * allow event based instantiation of the other BotWorks Bot classes.
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-15
 */
public class BotWorks {
    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
    static String factory_bot_config_file_with_path = null;
    static String producer_config_file_with_path = null;
    static String log4j_config_file_with_path = null;
    static String message_listener_config_file_with_path = null;

    public static void main(String[] args) throws Exception {
        BotWorks botWorks = new BotWorks();

        // getting configurations from the config file passed in arguments
        if (args.length == 1) {
            String botworksConfigurationAsString = FileUtils.readFileToString(new File(args[0]),
                    Charset.defaultCharset());
            Map<String, Object> botworksConfigurationMap = JSONUtils.jsonToMap(botworksConfigurationAsString);
            log4j_config_file_with_path = botworksConfigurationMap.get(BotCommon.LOG4J_CONFIG_FILE_WITH_PATH).toString();
            factory_bot_config_file_with_path = botworksConfigurationMap.get(BotCommon.BOTFACTORY_CONFIG_FILE).toString();
            producer_config_file_with_path = botworksConfigurationMap.get(BotCommon.PRODUCER_CONFIG_FILE_PATH).toString();
            message_listener_config_file_with_path = botworksConfigurationMap.get(BotCommon.MESSAGE_LISTENER_CONFIG_FILE_WITH_PATH).toString();
        } else {
            botWorks.logger.error("BotWorks is not supplied with configurations directory path");
            botWorks.logger.error("So Couldn't start the BotWorks process !");
            System.exit(1);
        }

        // load the log4j properties file
        try {
            Configurator.initialize(null, log4j_config_file_with_path);
            botWorks.logger.info("logger config file is  found " + log4j_config_file_with_path);
        } catch (Exception ex) {
            ex.printStackTrace();
            botWorks.logger.error("logger config file i.e log4j2.xml is not found !" + log4j_config_file_with_path);
        }


        //launch factory bot
        launchFactoryBot(factory_bot_config_file_with_path);

        //spawn MessageListener to listen all bot eco system messages to log at high level to file appender
        spawnMessageListener();

//        //sleep for a minute to launch message to FactoryBot for it to start the other Bots
//        Thread.sleep(TimeUnit.SECONDS.toMillis(60));

        //send KAFKA message to up/spawn the bots as configured in the config file
        spawnInitialBots();

    }

    /**
     * This method launches the FactoryBot with the configs supplied in configuration file
     *
     * @param config_file
     */
    public static void launchFactoryBot(String config_file) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    String jsonConfig = FileUtils.readFileToString(new File(config_file), "UTF-8");
                    HashMap<String, Object> map = JSONUtils.jsonToMap(jsonConfig);
                    //Provide the Bot class which shall be made up
                    String botClassName = (String) map.get(BotCommon.BOT_CLASSNAME);
                    Bot bot = (Bot) Class.forName(botClassName).newInstance();
                    bot.initBot(map);
                    bot.startBot();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * This method spawns the initial bots which are configured in the message to BotFactory
     */
    public static void spawnInitialBots() {
        SpawnBotsUtility spawnBotsUtility = new SpawnBotsUtility();
        // Initialize producer
        try {
            HashMap<String, Object> props = JSONUtils.jsonToMap(FileUtils.readFileToString(
                    new File(producer_config_file_with_path),
                    Charset.defaultCharset()));
            spawnBotsUtility.initialize(props);
            // Publish message
            spawnBotsUtility.publishMesssage(props);
            spawnBotsUtility.closeKafkaProducer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method spawns a message listener in the bot eco system , useful in looking at high level events
     * Please refer logs from LogBot for more descriptive logging
     */
    public static void spawnMessageListener() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    MessageListener.doListening(message_listener_config_file_with_path);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }
}

