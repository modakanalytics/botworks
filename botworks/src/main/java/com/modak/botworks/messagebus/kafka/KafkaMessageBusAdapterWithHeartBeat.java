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
import com.modak.botworks.messagebus.BasicMessageBusAdapter;
import com.modak.botworks.messagebus.BotsMessageEncryptionUtils;
import com.modak.botworks.messagebus.MessageBusException;
import com.modak.botworks.messagebus.MessageReceiver;
import com.modak.encryption.RSAEncryptionUtils;
import com.modak.utils.*;
import com.modak.utils.MessageUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;

import java.io.StreamCorruptedException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

/**
 * This class gives the Kafka implementation of BasicMessageBusAdapter class for the Pub/Sub type model
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-02
 */

public class KafkaMessageBusAdapterWithHeartBeat extends BasicMessageBusAdapter {

    private final org.apache.logging.log4j.Logger logger = LogManager
        .getLogger(this.getClass().getSimpleName());
    /**
     * List of KafkaClient object that is connected to the broker.
     */

    protected List<KafkaConsumer> kafkaConsumers = null;

    //***********************************************************************
    // Protected methods and data
    //***********************************************************************

    protected KafkaProducer kafkaProducer = null;
    protected MessageReceiver messageReceiver = null;
    protected boolean pull = true;
    protected boolean shutdown = false;
    protected boolean isPause = false;
    protected PublicKey publicKey = null;
    protected PrivateKey privateKey = null;
    protected int publishMessageSize = 0;
    protected boolean kafkaConsumersPaused = true;
    protected String botClassName;
    protected String botUUID;
    protected ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    protected String trustStoreFilePath;

    public KafkaMessageBusAdapterWithHeartBeat() {

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
            kafkaConsumers = new ArrayList<KafkaConsumer>();
            //get public and private keys
            publicKey = RSAEncryptionUtils.getPublicKey(MessageUtils.getString(props, BotCommon.PUBLIC_KEY));
            privateKey = RSAEncryptionUtils.getPrivateKey(MessageUtils.getString(props, BotCommon.PRIVATE_KEY));

            // Getting the bootstrap server names from the config
            String bootstrap_servers = MiscUtils.getProperty(props, BotCommon.KAFKA_BOOTSTRAP_SERVERS);
            pull = Boolean.valueOf(MiscUtils.getProperty(props, BotCommon.KAFKA_DELIVERY));
            //get max message size
            publishMessageSize = (Integer) props.get(BotCommon.BOT_PUBLISH_MESSAGE_SIZE);

            //use below configs if Kafka is being used on SSL
            //trustStoreFilePath = props.get(BotCommon.TRUSTORE_FILE_PATH).toString();

            botClassName = props.get(BotCommon.BOT_TYPE) != null ? props.get(BotCommon.BOT_TYPE).toString() : "";
            botUUID = props.get(BotCommon.BOT_UUID) != null ? props.get(BotCommon.BOT_UUID).toString() : "";

            ArrayList<HashMap<String, Object>> priorityTopics = priorityTopicsSort(
                MessageUtils.getArrayList(props, BotCommon.PRIORITY_TOPIC_LIST));
            for (HashMap<String, Object> topicsList : priorityTopics) {
                //unique client ID generated every time kafka client is configured
                String clientId = MiscUtils.getProperty(props, BotCommon.KAFKA_CLIENT_ID, UUID.randomUUID().toString());

                Properties kafkaConsumerProps = new Properties();
                // Kafka Bootstrap servers / Worker nodes hostnames
                kafkaConsumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap_servers);
                // Configuring the kafka group name for the consumer
                kafkaConsumerProps
                    .put(ConsumerConfig.GROUP_ID_CONFIG,
                        MiscUtils.getProperty(topicsList, BotCommon.BOT_CONTROL_GROUP_ID));
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

//                kafkaConsumerProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, BotCommon.SECURITY_PROTOCOL_TYPE);
//                kafkaConsumerProps.put(SaslConfigs.SASL_MECHANISM, BotCommon.MECHANISM);
//                kafkaConsumerProps.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, BotCommon.SERVICE_NAME);
//                kafkaConsumerProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStoreFilePath);

                ArrayList<String> topics = MessageUtils.getArrayList(topicsList, "topics");
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
                    }
                    else {
                        kafkaConsumer.subscribe(topics);
                        logger.info("Kafka consumer subscribed to topic : {}", topics);
                    }
                    kafkaConsumers.add(kafkaConsumer);
                }
            }
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
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            throw new MessageBusException(e);
        }
    }

    /**
     * priorityTopicsSort method dorts the topics based on the provided priority
     *
     * @param topics Specifies all the priority topics that the client module is listening.
     * @return the sorted topics based on priority
     */
    protected ArrayList<HashMap<String, Object>> priorityTopicsSort(ArrayList<HashMap<String, Object>> topics) {
        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                return Integer.parseInt(m1.get(BotCommon.PRIORITY).toString()) - Integer
                    .parseInt(m2.get(BotCommon.PRIORITY).toString());
            }
        };
        Collections.sort(topics, mapComparator);
        return topics;
    }

    /**
     * kafka consumer subscribes to the given list of topics with the default Quality of Service
     *
     * @param topics Specifies all the topics that the client module is listening.
     * @param QualityOfService The meaning of this parameter will vary with the  underlying messaging infrastructure
     * @param kafkaConsumer A kafkaConsumer that contains consumers
     * @throws Exception if there is error in subscribing to the topics
     */
    @Override
    public void subscribe(List<String> topics, int QualityOfService, KafkaConsumer kafkaConsumer)
        throws MessageBusException {
        checkKafkaInitStatus();
        if (topics != null) {
            try {
                kafkaConsumer.subscribe(topics);
                kafkaConsumers.add(kafkaConsumer);
            }
            catch (Exception e) {
                logger.error(e.toString());
                throw new MessageBusException(e);
            }
        }
    }

    /**
     * kafka consumer subscribes to the given list of topics
     *
     * @param topics Specifies all the topics that the client module is listening.
     * @param kafkaConsumer A kafkaConsumer that contains consumers
     * @throws Exception if there is error in subscribing to the topics
     */
    @Override
    public void subscribe(List<String> topics, KafkaConsumer kafkaConsumer) throws MessageBusException {
        checkKafkaInitStatus();
        if (topics != null) {
            try {
                kafkaConsumer.subscribe(topics);
                kafkaConsumers.add(kafkaConsumer);
            }
            catch (Exception e) {
                logger.error(e.toString());
                throw new MessageBusException(e);
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
            logger.debug("Message published topic = {} , decryptedMessage = {} ", topic,
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
            logger.debug("Message published topic = {} , decryptedMessage = {} ", topic,
                mesg != null ? MiscUtils.formatString(new String(mesg, StandardCharsets.UTF_8), true) : null);
        }
        catch (Exception e) {
            logger.error(e.toString());
            throw new MessageBusException(e);
        }
    }

    /**
     * Checks whether the kafka consumer or kafka producer is configured or not
     *
     * @throws MessageBusException throws exception if kafka consumer or kafka producer is not configured
     */
    private void checkKafkaInitStatus() throws MessageBusException {
        if (kafkaConsumers == null || kafkaProducer == null) {
            MessageBusException e = new MessageBusException(
                "Kafka client not initialized. Call initMessageBus method.");
            logger.error(e);
            throw new MessageBusException(e);
        }
    }

    /**
     * Check if message receiver is null and sends the message
     *
     * @param topic The underlying message system needs to provide a topic associated with the message payload.
     * @param mesg The payload of the message as a byte array.  Note that if the message
     * @throws Exception if message receiver is null
     */
    @Override
    public void messageArrived(String topic, byte[] mesg) throws MessageBusException {
        if (messageReceiver != null) {
            try {
                messageReceiver.messageArrived(topic, mesg);
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
     * This allows the client to register and potentially authenticate so that it can send and receive messages to/from
     * the underlying infrastructure
     *
     * @throws MessageBusException if the adapter is unable to register, then it should throw this exception.
     */
    @Override
    public void register(MessageReceiver messageReceiver) throws MessageBusException {
        if (messageReceiver != null) {
            this.messageReceiver = messageReceiver;
            if (messageReceiver == null) {
                logger.error("Message receiver configuration failed");
            }
        }
        else {
            throw new MessageBusException("Bot is null");
        }
        // when pull == false
        // start the message polling for push mode
        if (pull == false) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (shutdown == false) {
                        //If isPause is set to true then Polling stops until isPause is set to false
                        if (isPause == true) {
                            try {
                                Thread.sleep(3000);
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            boolean isMessageArrived = false;
                            for (KafkaConsumer kafkaConsumer : kafkaConsumers) {
                                ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(100);
                                logger.trace("Number of records received : {}, Bot : {}, Bot UUID: {}", records.count(),
                                    botClassName, botUUID);
                                for (ConsumerRecord<String, byte[]> record : records) {
                                    byte[] kafkaMessage = record.value();
                                    try {
                                        isMessageArrived = true;
                                        byte[] decryptedMessageBtArray = BotsMessageEncryptionUtils
                                            .decryptAndUncompressByteArray(kafkaMessage, privateKey);
                                        logger.debug(
                                            "Message received topic = {}, offset = {}, partition = {},\n decryptedMessage = {} ",
                                            record.topic(), record.offset(), record.partition(),
                                            decryptedMessageBtArray != null ? MiscUtils
                                                .formatString(
                                                    new String(decryptedMessageBtArray, StandardCharsets.UTF_8),
                                                    true) : null);

                                        messageArrived(record.topic(), decryptedMessageBtArray);
                                    }
                                    catch (StreamCorruptedException sce) {
                                        logger.error(sce.toString());
                                    }
                                    catch (Exception ue) {
                                        logger.error(ue.toString());
//                                    try {
//                                        shutdown();
//                                    }
//                                    catch (Exception e) {
//                                        logger.error(e.toString());
//                                    }
                                    }
                                    kafkaConsumer.commitSync();
                                }
                                if (isMessageArrived) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }).start();
        }

        // when pull == true
        // start the message polling for pull mode
        if (pull == true) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    pauseKafkaConsumers(); //pause until Bot is ready to consume messages
                    while (shutdown == false) {
                        //If isPause is set to true then Polling stops until isPause is set to false
                        if (isPause == true) {
                            try {
                                Thread.sleep(3000);
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            if (kafkaConsumersPaused == false) {
                                resumeKafkaConsumers();
                            }
                            try {
                                Thread.sleep(200);
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            for (KafkaConsumer kafkaConsumer : kafkaConsumers) {
                                ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(100);
                                logger.trace("Number of records received : {}, Bot : {}, Bot UUID: {}", records.count(),
                                    botClassName, botUUID);
                                for (ConsumerRecord<String, byte[]> record : records) {
                                    //logger.debug("Record is being processed : {}" , record.value());
                                    //logger.info(kafkaConsumer + " Priority Topic Number : " + checkPriorityOrder);
                                    byte[] kafkaMessage = record.value();
                                    kafkaConsumer.commitSync();
                                    try {
                                        byte[] bytes = BotsMessageEncryptionUtils
                                            .decryptAndUncompressByteArray(kafkaMessage, privateKey);
                                        String decryptedMessage =
                                            bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null;
                                        logger.debug(
                                            "Message received topic = {}, offset = {}, partition = {},\n decryptedMessage = {} ",
                                            record.topic(), record.offset(), record.partition(),
                                            MiscUtils.formatString(decryptedMessage, true));
                                        messageQueue.add(decryptedMessage);
                                        // tell the Kafka broker that the message fetching will be stopped for a while. We will only do polling so that heartbeat is still sent without getting any messages.
                                        pauseKafkaConsumers();
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                    if (kafkaConsumers != null) {
                        for (KafkaConsumer kafkaConsumer : kafkaConsumers) {
                            kafkaConsumer.close();
                        }
                    }
                    if (kafkaProducer != null) {
                        kafkaProducer.close();
                    }
                    logger.debug("Bot SHUTDOWN Done for Bot : {}, Bot UUID : {}", botClassName, botUUID);
                }
            }).start();
        }
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
            for (KafkaConsumer kafkaConsumer : kafkaConsumers) {
                kafkaConsumer.unsubscribe();
            }
        }
        catch (Exception e) {
            logger.error(e.toString());
            throw new MessageBusException(e);
        }
    }

    /**
     * Fetches the message by polling the kafka server for the specified kafka consumer and returns the obtained
     * message
     *
     * @return the kafka message obtained by polling on specified topic
     * @throws MessageBusException throws MessageBusException
     */
    public String fetchMessage() throws MessageBusException {

        // resume the reading of the messages
        setKafkaConsumersPaused(false);
        boolean gotMessage = false;
        while (!gotMessage) {
            try {
                Thread.sleep(300);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            String message = messageQueue.poll();
            if (message != null) {
                gotMessage = true;
                return message;
            }
        }
        return null;
    }


    /**
     * This will terminate the kafka consumer and cleans up any resources used by the adapter.
     */
    @Override
    public void shutdown() throws Exception {
        shutdown = true;
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e) {
            logger.error("Exception occurred while trying to sleep");
        }
    }

    /**
     * This will pause the kafka consumer fetchMessages
     */
    @Override
    public void pause() throws Exception {
        this.isPause = true;
        Thread.sleep(3000);
    }

    /**
     * This will resumes the kafka consumer fetchMessages
     */
    @Override
    public void resume() throws Exception {
        this.isPause = false;
        Thread.sleep(3000);
    }

    /**
     * This gets the pause status
     *
     * @throws Exception If unable to terminate, then an Exception is thrown.
     */
    @Override
    public boolean isPaused() throws Exception {
        return isPause;
    }

    /**
     * containsPattern method check whether topic contains .* or not
     *
     * @param topics Specifies all the topics that the client module is listening.
     * @return true if topic contains .* else false
     */
    protected boolean containsPattern(ArrayList<String> topics) {
        for (String topic : topics) {
            if (topic.contains(".*")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Pauses the Kafka Consumers using their respective assignments. Basically, we are sending communication to Kafka
     * broker that we will not be consuming more messages This is needed, so that we can poll for messages without
     * actually fetching any message. The heartbeat mechanism of KafkaConsumer might be depended on the Polling.
     */
    protected synchronized void pauseKafkaConsumers() {
        if (kafkaConsumers != null) {
            for (KafkaConsumer kafkaConsumer : kafkaConsumers) {
                kafkaConsumer.pause(kafkaConsumer.assignment());
            }
        }
        kafkaConsumersPaused = true;
    }

    /**
     * Resumes the Kafka Consumers. Read the 'pauseKafkaConsumers' documentation.
     */
    protected synchronized void resumeKafkaConsumers() {
        if (kafkaConsumers != null) {
            for (KafkaConsumer kafkaConsumer : kafkaConsumers) {
                kafkaConsumer.resume(kafkaConsumer.assignment());
            }
        }
        kafkaConsumersPaused = false;
    }


    /**
     * Resumes the Kafka Consumers. Read the 'pauseKafkaConsumers' documentation.
     */
    protected synchronized void setKafkaConsumersPaused(boolean paused) {
        kafkaConsumersPaused = paused;
    }


}
