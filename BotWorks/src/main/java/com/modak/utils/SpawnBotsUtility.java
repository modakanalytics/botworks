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
import com.modak.compression.NullCompressor;
import com.modak.encryption.EncryptionUtils;
import com.modak.encryption.RSAEncryptionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.PublicKey;
import java.util.*;

/**
 * This utility class sends a bot the message to a factory bot to initialize and spawn various bots
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-02
 */

public class SpawnBotsUtility {
    
    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

    private KafkaProducer producer;
    private NullCompressor nullCompressor = new NullCompressor();

    /**
     * Initializes the Kafka producers with the configuration provided
     *
     * @param props passing configurations path for .json file
     * @throws IOException if the input or output operation is failed or interpreted.
     */
    public void initialize(Map<String, Object> props) throws IOException {
        Properties kafkaProducerProps = new Properties();
        kafkaProducerProps
                .put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.get(BotCommon.BOOTSTRAP_SERVERS_CONFIG));
        kafkaProducerProps
                .put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, props.get(BotCommon.KEY_SERIALIZER_CLASS_CONFIG));
        kafkaProducerProps
                .put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, props.get(BotCommon.VALUE_SERIALIZER_CLASS_CONFIG));
        kafkaProducerProps.put(ProducerConfig.ACKS_CONFIG, props.get(BotCommon.ACKS_CONFIG));
        producer = new KafkaProducer(kafkaProducerProps);
    }

    /**
     * producer_config_spawn_initial_bots.json is read and the same is send as a message to FactoryBot to spawn bots which are configured
     *
     * @param props to initialize Kafka Producer.
     * @throws Exception if the method operation is failed or interpreted
     */
    public void publishMesssage(HashMap props) throws Exception {

        List<String> topics = MessageUtils.getArrayList(props, BotCommon.BOT_FACTORY_TOPICS);

        long process_id = System.currentTimeMillis();
        HashMap<String, Object> data = new HashMap<>();
        data.put(BotCommon.PROCESS_ID, process_id);
        data.put(BotCommon.MSG_UUID, UUID.randomUUID());

        String msg = StringTemplateUtils
            .renderTemplate(MessageUtils.getString(props, BotCommon.TEMPLATE_GROUP), MessageUtils.getString(props, BotCommon.TEMPLATE_NAME), data);
        String inputpublickey = props.get(BotCommon.PUBLIC_KEY_PATH).toString();
        //compress and encrypt the json string
        byte[] compressedBytes = nullCompressor.compressString(msg);
        PublicKey publicKey = RSAEncryptionUtils.getPublicKey(inputpublickey);
        byte[] encryptedBytes = EncryptionUtils.encryptLargeByteArrayUsingRSA(compressedBytes, publicKey);
        for (String topic : topics) {
            producer.send(new ProducerRecord(topic, encryptedBytes));
            logger.debug(BotCommon.MESSAGE_PUBLISHED, msg);
            logger.debug(BotCommon.PROCESS_ID_PUBLISHED_ON_TOPIC_MESSAGE_SIZE_INFO, process_id, topic, encryptedBytes.length);
        }
    }

    /**
     * shutdowns the Kafka
     */
    public void closeKafkaProducer() {
        if (producer != null) {
            //Close the producer
            producer.close();
        }
    }
}
