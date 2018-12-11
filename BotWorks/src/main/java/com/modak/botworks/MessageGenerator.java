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
import com.modak.compression.NullCompressor;
import com.modak.encryption.EncryptionUtils;
import com.modak.encryption.RSAEncryptionUtils;
import com.modak.utils.JSONUtils;
import com.modak.utils.MessageUtils;
import com.modak.utils.StringTemplateUtils;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * FireMessageToBotCommon is a utility class which can send a sample message to a bot as defined in producer_config_fire_message_to_bot.json file
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2018-11-21
 */
public class MessageGenerator {

    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
    private KafkaProducer producer;
    HashMap<String, Object> props;
    String botInitialMessage = null;

    private NullCompressor nullCompressor = new NullCompressor();

    static String producer_config_file_with_path = null;
    static String log4j_config_file_with_path = null;


    /**
     *  This method publishes the message to the Kafka world
     */
    public void publishMessage() {
        try {
            HashMap<String, Object> templateInputs = new HashMap<>();
            templateInputs.put(BotCommon.DATA, MessageUtils.getMap(props, BotCommon.INFORMATION_OF_SOURCE_TO_STREAM));
            templateInputs.put(BotCommon.PROCESS_ID,System.currentTimeMillis());
            botInitialMessage = StringTemplateUtils.renderTemplate(MessageUtils.getString(props, BotCommon.BOT_MESSAGE_TEMPLATE_GROUP), MessageUtils.getString(props, BotCommon.BOT_MESSAGE_TEMPLATE_NAME), templateInputs);
            byte[] compressedBytes = nullCompressor.compressString(botInitialMessage);
            PublicKey publicKey = RSAEncryptionUtils.getPublicKey(props.get(BotCommon.PUBLICKEY_PATH).toString());
            byte[] encryptedBytes = EncryptionUtils.encryptLargeByteArrayUsingRSA(compressedBytes, publicKey);
            producer.send(new ProducerRecord(props.get(BotCommon.TOPIC_CONFIG_ATTRIBUTE).toString(), encryptedBytes));
            logger.info(BotCommon.BOT_INITIAL_MESSAGE_PUBLISHED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This message builds a Kafka Producer from the configs supplied
     */
    public void buildKafkaProducer() {
        try {
            // Initialize producer
            props = JSONUtils.jsonToMap(FileUtils.readFileToString(
                    new File(producer_config_file_with_path),
                    Charset.defaultCharset()));

            Properties kafkaProducerProps = new Properties();
            kafkaProducerProps
                    .put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.get(BotCommon.BOOTSTRAP_SERVERS_CONFIG));
            kafkaProducerProps
                    .put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, props.get(BotCommon.KEY_SERIALIZER_CLASS_CONFIG));
            kafkaProducerProps
                    .put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, props.get(BotCommon.VALUE_SERIALIZER_CLASS_CONFIG));
            kafkaProducerProps.put(ProducerConfig.ACKS_CONFIG, props.get(BotCommon.ACKS_CONFIG));
            producer = new KafkaProducer(kafkaProducerProps);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * shutdowns the Kafka Producer
     */
    public void closeKafkaProducer() {
        if (producer != null) {
            //Close the producer
            producer.close();
        }
    }

    public static void main(String[] args) throws Exception {
        MessageGenerator messageGenerator = new MessageGenerator();
        // getting configurations files path from arguments
        if (args.length == 1) {
            String botworksConfigurationAsString = FileUtils.readFileToString(new File(args[0]),
                    Charset.defaultCharset());
            Map<String, Object> botworksConfigurationMap = JSONUtils.jsonToMap(botworksConfigurationAsString);
            log4j_config_file_with_path = botworksConfigurationMap.get(BotCommon.LOG4J_CONFIG_FILE_WITH_PATH).toString();
            producer_config_file_with_path = botworksConfigurationMap.get(BotCommon.PRODUCER_CONFIG_FILE_PATH).toString();

        } else {
            messageGenerator.logger.error(BotCommon.LOG_MSG_ARGS_ERROR);
            System.exit(1);
        }
        // load the log4j properties file
        try {
            Configurator.initialize(null, log4j_config_file_with_path);
            messageGenerator.logger.info(BotCommon.LOGGER_CONFIG_FILE_IS_FOUND_INFO, log4j_config_file_with_path);
        } catch (Exception ex) {
            messageGenerator.logger.error(BotCommon.LOGGER_CONFIG_FILE_LOG4J2_XML_IS_NOT_FOUND_INFO, log4j_config_file_with_path);
        }

        //build a kafka producer
        messageGenerator.buildKafkaProducer();
        //fire or publish the test message to a bot on a topic
        messageGenerator.publishMessage();
        //close the kafka producer
        messageGenerator.closeKafkaProducer();
    }
}
