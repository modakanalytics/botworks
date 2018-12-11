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

import com.modak.botworks.bots.common.BotCommon;
import com.modak.botworks.messagebus.BotsMessageEncryptionUtils;
import com.modak.encryption.RSAEncryptionUtils;
import com.modak.utils.JSONUtils;
import com.modak.utils.MessageUtils;
import com.modak.utils.MiscUtils;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.StreamCorruptedException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class is a listens for all the kafka bot messages in the bot eco system.
 * It is same as LogBot but this class will parse the bot messages and logs only the
 * summary of the things happening in the bot messages. Useful for high level console viewing of messages or bot activity
 *
 * @author modakanalytics
 * @version 1.0
 * @since 12/5/2018
 */
public class MessageListener {

    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
    HashMap<String, Object> props;
    static String consumer_config_file_with_path = null;
    static String log4j_config_file_with_path = null;
    protected PrivateKey privateKey = null;
    protected List<KafkaConsumer> kafkaConsumers = new ArrayList<>();

    /**
     * This method parses the bot message to get high level information
     * @param message
     */
    private void parseMessage(String message) {
        HashMap<String, Object> messageMap = JSONUtils.jsonToMap(message);
        String process_context = MessageUtils
                .getString(messageMap, BotCommon.PROCESS_CONTEXT) != null ? MessageUtils
                .getString(messageMap, BotCommon.PROCESS_CONTEXT) : null;
        String bot = MessageUtils.getString(messageMap, BotCommon.BOT_TYPE) != null ? MessageUtils
                .getString(messageMap, BotCommon.BOT_TYPE) : null;
        logger
                .debug(BotCommon.LOG_MESSAGE, bot, process_context, getReceivedTime(System.currentTimeMillis()));
    }

    private String getReceivedTime(Long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(BotCommon.TIME_PATTERN);
        Date resultdate = new Date(time);
        return sdf.format(resultdate);
    }

    /**
     * This method listens to the bot messages in bot ecosystem and then sends it to parsing method
     * @throws Exception
     */
    private void listenMessages() throws Exception {
        privateKey = RSAEncryptionUtils.getPrivateKey(MessageUtils.getString(props, BotCommon.PRIVATE_KEY));
        while (true) {
            for (KafkaConsumer kafkaConsumer : kafkaConsumers) {
                ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(100);

                for (ConsumerRecord<String, byte[]> record : records) {
                    byte[] kafkaMessage = record.value();
                    try {
                        byte[] decryptedMessageBtArray = BotsMessageEncryptionUtils
                                .decryptAndUncompressByteArray(kafkaMessage, privateKey);

                        String message = new String(decryptedMessageBtArray, StandardCharsets.UTF_8);
                        parseMessage(message);

                    } catch (StreamCorruptedException sce) {
                        logger.error(sce.toString());
                    } catch (Exception ue) {
                        logger.error(ue.toString());
                    }
                    kafkaConsumer.commitSync();
                }
            }
        }
    }

    /**
     * This method builds Kafka consumer with the supplied Kafka config parameters
     */
    public void buildKafkaConsumer() {
        try {
            props = JSONUtils.jsonToMap(
                    FileUtils.readFileToString(new File(consumer_config_file_with_path), Charset.defaultCharset()));
            // Initialize consumer
            String clientId = MiscUtils.getProperty(props, BotCommon.KAFKA_CLIENT_ID, UUID.randomUUID().toString());

            Properties kafkaConsumerProps = new Properties();
            // Kafka Bootstrap servers / Worker nodes hostnames
            kafkaConsumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    MessageUtils.getString(props, BotCommon.KAFKA_BOOTSTRAP_SERVERS));
            // Configuring the kafka group name for the consumer
            kafkaConsumerProps
                    .put(ConsumerConfig.GROUP_ID_CONFIG,
                            MiscUtils.getProperty(props, BotCommon.BOT_CONTROL_GROUP_ID));
            // Enabling the Auto commit true for the consumer
            kafkaConsumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            // setting the session timeout to 30000ms
            kafkaConsumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000");
            // Setting number of records to be polled at once = 1
            kafkaConsumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
            // Setting the random generated Client ID to the kafka consumer
            kafkaConsumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
            // Serializing the consumer key using org.apache.kafka.common.serialization.StringDeserializer
            kafkaConsumerProps
                    .put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                            "org.apache.kafka.common.serialization.StringDeserializer");
            // Serializing the consumer value using org.apache.kafka.common.serialization.StringDeserializer
            kafkaConsumerProps
                    .put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                            "org.apache.kafka.common.serialization.ByteArrayDeserializer");

            kafkaConsumerProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 172800000);// 2days 172800000
            kafkaConsumerProps.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 172830000);// 2days 5sec 172830000
            ArrayList<String> topics = MessageUtils.getArrayList(props, "topics");
            KafkaConsumer kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProps);
            if (topics instanceof ArrayList) {
                if (containsPattern(topics)) {
                    for (String topic : topics) {
                        kafkaConsumer.subscribe(Pattern.compile(topic), new ConsumerRebalanceListener() {
                            @Override
                            public void onPartitionsRevoked(Collection<TopicPartition> collection) {
                            }

                            @Override
                            public void onPartitionsAssigned(Collection<TopicPartition> collection) {
                            }
                        });
                        logger.info("Kafka consumer subscribed to topic : {} ", topics);
                    }
                } else {
                    kafkaConsumer.subscribe(topics);
                    logger.info("Kafka consumer subscribed to topic : {}", topics);
                }
                kafkaConsumers.add(kafkaConsumer);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected boolean containsPattern(ArrayList<String> topics) {
        for (String topic : topics) {
            if (topic.contains(".*")) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method listens builds Kafka consumer and listens to the Kafka messages in bot ecosystem and then parses the messages to get high level information
     * @param message_listener_config_file_with_path
     * @throws Exception
     */
    public static void doListening(String message_listener_config_file_with_path) throws Exception {
        MessageListener logParser = new MessageListener();
        // getting configurations files path from arguments
        if (message_listener_config_file_with_path!=null) {
            Map<String, Object> consumerConfigMapForMessageListener = JSONUtils.jsonToMap(FileUtils.readFileToString(new File(message_listener_config_file_with_path),
                    Charset.defaultCharset()));
            log4j_config_file_with_path = consumerConfigMapForMessageListener.get(BotCommon.LOG4J_CONFIG_FILE_WITH_PATH)
                    .toString();
            consumer_config_file_with_path = consumerConfigMapForMessageListener.get(BotCommon.CONSUMER_CONFIG_FILE_PATH)
                    .toString();
        } else {
            logParser.logger.error(BotCommon.MESSAGE_LISTENER_LOG_MSG_ARGS_ERROR);
            System.exit(1);
        }
        // load the log4j properties file
        try {
            Configurator.initialize(null, log4j_config_file_with_path);
            logParser.logger.info(BotCommon.LOGGER_CONFIG_FILE_IS_FOUND_INFO, log4j_config_file_with_path);
        } catch (Exception ex) {
            logParser.logger
                    .error(BotCommon.LOGGER_CONFIG_FILE_LOG4J2_XML_IS_NOT_FOUND_INFO, log4j_config_file_with_path);
        }

        //build a kafka consumer
        logParser.buildKafkaConsumer();
        //listen to the bot eco system messages and log using file appender
        logParser.listenMessages();
    }
}
