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

package com.modak.botworks.messagebus.kafka;

import com.modak.botworks.bots.common.BotCommon;
import com.modak.botworks.messagebus.BotControlMessageChannel;
import com.modak.botworks.messagebus.BotsMessageEncryptionUtils;
import com.modak.botworks.messagebus.ControlMessageBusAdapter;
import com.modak.botworks.messagebus.MessageBusException;
import com.modak.encryption.RSAEncryptionUtils;
import com.modak.utils.*;
import com.modak.utils.MessageUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;

import java.io.StreamCorruptedException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class gives the Kafka implementation of ControlMessageBusAdapter for the Pub/Sub type model
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2018-02-12
 */

public class KafkaBotControlMessageBusAdapter implements ControlMessageBusAdapter {

    private final org.apache.logging.log4j.Logger logger = LogManager
        .getLogger(this.getClass().getSimpleName());

    /**
     * The KafkaClient object that is connected to the broker.
     */
    protected KafkaConsumer kafkaConsumer = null;

    //***********************************************************************
    // Protected methods and data
    //***********************************************************************

    protected KafkaProducer kafkaProducer = null;
    protected boolean pull = true;
    protected boolean shutdown = false;
    protected BotControlMessageChannel botControlMessageChannel = null;
    protected PublicKey publicKey = null;
    protected PrivateKey privateKey = null;
    protected int publishMessageSize = 0;
    protected String botClassName;
    protected String botUUID;
    protected String consumerGroupId;
    protected String controlTopic;
    protected String trustStoreFilePath;

    public KafkaBotControlMessageBusAdapter() {

    }

    /**
     * Provides Kafka specific configuration that is needed to provide the services of the underlying messaging system.
     * It initializes Kafka message bus with the supplied properties / configurations
     *
     * @param props A set of key-value pairs that contain Kafka infrastructure specific information.
     * @throws Exception if there is any error in intialising kafka producer and consumer
     */
    @Override
    public void initMessageBus(HashMap<String, Object> props) throws MessageBusException {
        try {
            pull = Boolean.valueOf(MiscUtils.getProperty(props, BotCommon.KAFKA_DELIVERY));
            //get public and private keys
            publicKey = RSAEncryptionUtils.getPublicKey(MessageUtils.getString(props, BotCommon.PUBLIC_KEY));
            privateKey = RSAEncryptionUtils.getPrivateKey(MessageUtils.getString(props, BotCommon.PRIVATE_KEY));

            //get max message size
            publishMessageSize = (Integer) props.get(BotCommon.BOT_PUBLISH_MESSAGE_SIZE);

            //trustStoreFilePath = props.get(BotCommon.TRUSTORE_FILE_PATH).toString();

            botClassName = props.get(BotCommon.BOT_TYPE) != null ? props.get(BotCommon.BOT_TYPE).toString() : "";
            botUUID = props.get(BotCommon.BOT_UUID) != null ? props.get(BotCommon.BOT_UUID).toString() : "";

            //unique client ID generated every time kafka client is configured
            String clientId = MiscUtils.getProperty(props, BotCommon.KAFKA_CLIENT_ID, UUID.randomUUID().toString());
            // Getting the bootstarp server names from the config
            String bootstrap_servers = MiscUtils.getProperty(props, BotCommon.KAFKA_BOOTSTRAP_SERVERS);
            Properties kafkaConsumerProps = new Properties();
            // Kafka Bootstrap servers / Worker nodes hostnames
            kafkaConsumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap_servers);
            // group_id will be random uuid every new bot
            consumerGroupId = BotCommon.CONTROL_TOPIC_GROUP_PREFIX + botUUID;
            // Configuring the kafka group name for the consumer
            kafkaConsumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
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

            //use below configs if Kafka is being used on SSL
            // kafkaConsumerProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, BotCommon.SECURITY_PROTOCOL_TYPE);
//            kafkaConsumerProps.put(SaslConfigs.SASL_MECHANISM, BotCommon.MECHANISM);
//            kafkaConsumerProps.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, BotCommon.SERVICE_NAME);
//            kafkaConsumerProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStoreFilePath);

            kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProps);

            Properties kafkaProducerProps = new Properties();
            // Configuring the Kafka producer bootstrap servers / worker nodes hostnames
            kafkaProducerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap_servers);
            // Serializing the producer key using org.apache.kafka.common.serialization.StringDeserializer
            kafkaProducerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
            // Serializing the producer value using org.apache.kafka.common.serialization.StringDeserializer
            kafkaProducerProps
                .put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.ByteArraySerializer");
            // acks = acknowledge, producer gives the acknowledge to the bootstrap servers / worker nodes after publishing a message
            kafkaProducerProps.put(ProducerConfig.ACKS_CONFIG, "1");

            //use below configs if Kafka is being used on SSL
//            kafkaProducerProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, BotCommon.SECURITY_PROTOCOL_TYPE);
//            kafkaProducerProps.put(SaslConfigs.SASL_MECHANISM, BotCommon.MECHANISM);
//            kafkaProducerProps.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, BotCommon.SERVICE_NAME);
//            kafkaProducerProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStoreFilePath);

            kafkaProducer = new KafkaProducer(kafkaProducerProps);

            controlTopic = MiscUtils.getProperty(props, BotCommon.CONTROL_TOPIC);
            subscribe(Arrays.asList(controlTopic));
            logger.info("groupId for bot : {}, controlTopic : {}, for Bot : {}, Bot UUID : {}", consumerGroupId,
                controlTopic, botClassName, botUUID);

        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            throw new MessageBusException(e);
        }
    }

    /**
     * kafka consumer subscribes to the given list of topics with the default Quality of Service
     *
     * @param topics Specifies all the topics that the client module is listening.
     * @param QualityOfService The meaning of this parameter will vary with the  underlying messaging infrastructure
     * @throws Exception if there is error in subscribing to the topics
     */
    @Override
    public void subscribe(List<String> topics, int QualityOfService) throws MessageBusException {
        subscribe(topics);
    }

    /**
     * kafka consumer subscribes to the given list of topics
     *
     * @param topics Specifies all the topics that the client module is listening.
     * @throws Exception if there is error in subscribing to the topics
     */
    @Override
    public void subscribe(List<String> topics) throws MessageBusException {
        checkKafkaInitStatus();
        if (topics != null) {
            if (containsPattern(topics)) {
                for (String topic : topics) {
                    //create topic using kafka utility
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
            }
            else {
                kafkaConsumer.subscribe(topics);
                logger.info("Kafka consumer subscribed to topic : {}", topics);
            }

        }
    }

    /**
     * Publishes the kafka message on a given topic
     *
     * @param topic The topic to use when publishing this message.
     * @param mesg The message payload.  If it contains binary type data it is expected that you Base64 encode the
     * data.
     * @throws Exception if there is error in publishing the message
     */
    @Override
    public void publish(String topic, byte[] mesg) throws MessageBusException {
        checkKafkaInitStatus();
        try {
            byte[] outputMessageBytes = BotsMessageEncryptionUtils.compressAndEncryptByteArray(mesg, publicKey);
            if (outputMessageBytes != null) {
                long size = outputMessageBytes.length / (1024L * 1024L);
                if (size > publishMessageSize) {
                    throw new MessageBusException(
                        BotCommon.BOT_PUBLISH_MESSAGE_SIZE_BYTES + outputMessageBytes.length + " Bytes "
                            + BotCommon.BOT_PUBLISH_MESSAGE_SIZE_EXCEPTION + size + " MB ");
                }
            }
            kafkaProducer.send(new ProducerRecord(topic, outputMessageBytes));
            logger.debug("ControlMessage published topic = {} , decryptedMessage = {} ", topic,
                mesg != null ? MiscUtils.formatString(new String(mesg, StandardCharsets.UTF_8), true) : null);
        }
        catch (Exception e) {
            logger.error(e.toString());
            throw new MessageBusException(e);
        }
    }

    /**
     * Publishes the kafka message on a given topic with default Quality of Service
     *
     * @param topic The topic to use when publishing this message.
     * @param mesg The message payload.  If it contains binary type data it is expected that you Base64 encode the
     * data.
     * @throws Exception if there is error in publishing the message
     */
    @Override
    public void publish(String topic, byte[] mesg, int qos) throws MessageBusException {
        publish(topic, mesg);  // Kafka doesn't implement priority so we are ignoring the priority
    }

    /**
     * This allows the client to register and potentially autenticate so that it can send and receive messages to/from
     * the underlying infrastructure
     *
     * @throws MessageBusException if the adapter is unable to register, then it should throw this exception.
     */
    @Override
    public void register(BotControlMessageChannel botControlMessageChannel) throws MessageBusException {
        if (botControlMessageChannel != null) {
            this.botControlMessageChannel = botControlMessageChannel;
        }
        else {
            throw new MessageBusException("Bot is null");
        }

        // start the message polling
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (shutdown == false) {
                    ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(100);
                    //logger.trace("Number of records received : {}, Bot : {}, Bot UUID: {}", records.count(), botClassName, botUUID);
                    for (ConsumerRecord<String, byte[]> record : records) {
                        byte[] kafkaMessage = record.value();
                        try {

                            String controlMessage = BotsMessageEncryptionUtils
                                .decryptAndUncompressString(kafkaMessage, privateKey);

                            logger.debug(
                                "ControlMessage received topic = {}, offset = {}, partition = {},decryptedMessageBtArray \n decryptedMessage = {} ",
                                record.topic(), record.offset(), record.partition(),
                                MiscUtils.formatString(controlMessage, true));
                            //parse the control message data
                            controlMessageArrived(controlMessage);
                        }
                        catch (StreamCorruptedException sce) {
                            logger.error(sce.toString());
                        }
                        catch (Exception ue) {
                            logger.error(ue.toString());
//                            try {
//                                shutdown();
//                            }
//                            catch (Exception e) {
//                                logger.error(e.toString());
//                            }
                        }
                        kafkaConsumer.commitSync();
                    }
                }
                kafkaConsumer.close();
                kafkaProducer.close();
                logger.debug(
                    "Bot Controller SHUTDOWN Done for uuid : {}, Bot : {} on controlTopic : {}, consumerGroupId : {}",
                    botUUID, botClassName, controlTopic, consumerGroupId);
            }
        }).start();

    }

    /**
     * Checks whether the kafka consumer or kafka producer is configured or not
     *
     * @throws MessageBusException throws exception if kafka consumer or kafka producer is not configured
     */
    private void checkKafkaInitStatus() throws MessageBusException {
        if (kafkaConsumer == null || kafkaProducer == null) {
            MessageBusException e = new MessageBusException(
                "Kafka client not initialized. Call initMessageBus method.");
            logger.error(e);
            throw new MessageBusException(e);
        }
    }

    /**
     * This function gets the controlFunction and passes the value to perform required operation on BOT
     *
     * @param controlMessage The underlying message system needs to provide a topic associated with the message
     * payload.
     * @throws Exception if message receiver is null
     */
    public void controlMessageArrived(String controlMessage) throws MessageBusException {
        if (botControlMessageChannel != null) {
            try {
                //Get control function from bot message
                String controlFunction = getControlFunction(controlMessage);
                if (controlFunction != null) {
                    //send controlFunction to perform action on bot
                    botControlMessageChannel.controlMessageArrived(controlFunction);
                }
            }
            catch (Exception e) {
                logger.error(e.toString());
            }
        }
        else {
            logger.error("Message receiver is null");
        }
    }


    /**
     * This method returns ControlFunction action to perform on BOT
     *
     * @param message is the one which receives by subscribing to topics
     * @return controlFunction
     */
    protected String getControlFunction(String message) throws Exception {
        String controlFunction = null;
        if (!MessageUtils.isMessageWellFormed(message)) {
            logger.error(BotCommon.PROBLEM_WITH_MESSAGE);
        }
        else {
            //Get ControlFunction from received bot message
            HashMap<String, Object> messageMap = JSONUtils.jsonToMap(message);
            ArrayList<HashMap<String, Object>> tasksMap = MessageUtils.getTasks(messageMap);
            for (HashMap<String, Object> taskmap : tasksMap) {
                HashMap<String, Object> processorMap = MessageUtils.getProcessor(taskmap);
                HashMap<String, Object> dataMap = MessageUtils.getTaskData(taskmap);
                HashMap<String, Object> processDataMapOutput = MessageUtils.processData(dataMap, processorMap);
                String forBotUUID = processDataMapOutput.get(BotCommon.BOT_CONTROL_UUID).toString();
                // only return the controlFunction if it is relevant to this Bot, else ignore it.
                if (forBotUUID.equalsIgnoreCase(botControlMessageChannel.getUUID())) {
                    controlFunction = processDataMapOutput.get(BotCommon.BOT_CONTROL_SWITCH).toString();
                }
            }
        }
        return controlFunction;
    }

    /**
     * Removes the subscription for one or more topics.
     *
     * @param topics A list of topics that should removed from active subscriptions
     * @throws MessageBusException If unable to perform the task, then it should throw this exception.
     */
    @Override
    public void unsubscribe(List<String> topics) throws MessageBusException {
        checkKafkaInitStatus();
        try {
            kafkaConsumer.unsubscribe();
        }
        catch (Exception e) {
            logger.error(e.toString());
            throw new MessageBusException(e);
        }
    }

    /**
     * Provides any infrastructure specific configuration that is needed to provide the services of the underlying
     * messaging system.
     *
     * @param configuration A JSON formatted document that contains implementation specific configuration information.
     */
    @Override
    public void initMessageBus(String configuration) throws MessageBusException {
        try {
            HashMap<String, Object> props = JSONUtils.jsonToMap(configuration);
            initMessageBus(props);
        }
        catch (MessageBusException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error(e.toString());
            throw new MessageBusException(e);
        }
    }

    /**
     * Provides any infrastructure specific configuration that is needed to provide the services of the underlying
     * messaging system.
     *
     * @param jsonObject A JSON object that contains implementation specific configuration information.
     */
    @Override
    public void initMessageBus(JSONObject jsonObject) throws MessageBusException {
        try {
            HashMap<String, Object> props = JSONUtils.jsonToMap(jsonObject);
            initMessageBus(props);
        }
        catch (MessageBusException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error(e.toString());
            throw new MessageBusException(e);
        }
    }

    /**
     * This will terminate the kafka consumer and cleans up any resources used by the adapter.
     *
     * @throws Exception If unable to terminate, then an Exception is thrown.
     */
    @Override
    public void shutdown() throws Exception {
        shutdown = true;
        checkKafkaInitStatus();
        Thread.sleep(3000);
    }

    /**
     * containsPattern method check whether topic contains .* or not
     *
     * @param topics Specifies all the topics that the client module is listening.
     * @return true if topic contains .* else false
     */
    protected boolean containsPattern(List<String> topics) {
        for (String topic : topics) {
            if (topic.contains(".*")) {
                return true;
            }
        }
        return false;
    }
}
