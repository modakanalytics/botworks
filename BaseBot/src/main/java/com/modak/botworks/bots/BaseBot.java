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
import com.modak.botworks.messagebus.BotControlMessageChannel;
import com.modak.botworks.messagebus.ControlMessageBusAdapter;
import com.modak.botworks.messagebus.MessageBusAdapter;
import com.modak.botworks.messagebus.MessageBusException;
import com.modak.utils.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;


/**
 * This abstract class declares all methods related to Bots except the processBotLogic() As this method will have
 * specific implementation for each of the Bots
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-05-29
 */

public abstract class BaseBot implements Bot, BotControlMessageChannel {

    private final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
    protected String UUIDString = UUID.randomUUID().toString();

    /**
     * BotStatus holds the Current status of bot
     */
    protected String botStatus = BotCommon.BOT_STATUS_IDLE;

    /**
     * This HashMap which holds the current executing Task Map
     */
    protected HashMap<String, Object> currentTaskMap = null;

    /**
     * This HashMap which holds the current executing Message Map
     */
    protected HashMap<String, Object> currentMessageMap = null;

    /**
     * A list of topics to which the Bot should subscribe.
     */
    protected List<HashMap<String, Object>> topics = null;

    /**
     * A list of controller topics to which the Bot should subscribe.
     */
    protected List<String> topicController = null;

    /**
     * This HashMap which holds the message bus configuration
     */
    protected HashMap<String, Object> mbmap = null;
    /**
     * The Bot will buffer messages in the queue prior to processing them.  <br><br> Note that the incoming message will
     * be deposited in the queue in one thread and the messages will be pulled for processing in a separate thread.
     */
    protected ExecutorService executors;
    /**
     * The variable pull value states whether to process the messages sequentially or many messages at a time
     * the pull is true the message is fetched, processed and a new message is fetched again if the pull is false the
     * messages are fetched continuously and each message will be processed by single thread
     */
    protected boolean pull = true;
    protected boolean shutdown = false;
    protected int botHeartBeatFrequency = 0;
    protected int errorCount = 0;
    protected int consecutiveErrorLimit = 0;
    protected String botClassName = null;
    /**
     * This HashMap which holds the cot controller configuration
     */
    HashMap<String, Object> bcmap = null;
    private MessageBusAdapter messageBusAdapter = null;
    private ControlMessageBusAdapter controllerMessageBusAdapter = null;

    /**
     * A convenience method to allow the BaseBot to be initialized using a JSON formatted String object. <p> This should
     * be a valid JSON document that contains the parameters and information needed by a specific Bot implementation in
     * order to configure and register the Bot with the ecosystem.
     *
     * @throws Exception If the Bot cannot be initialized.
     */
    @Override
    public void initBot(String config) throws Exception {
        //Using JSONUtils class to convert the string configuration supplied into HashMap
        HashMap<String, Object> props = JSONUtils.jsonToMap(config);
        initBot(props);
    }

    /**
     * A convenience method to allow the Bot to be initialized using a JSON  object. <p> This should be a valid JSON
     * Object that contains the parameters and information needed by a specific Bot implementation in order to configure
     * and register the Bot with the ecosystem.
     *
     * @throws Exception If the Bot cannot be initialized.
     */
    @Override
    public void initBot(JSONObject jsonObject) throws Exception {
        //Using JSONUtils class to convert the JSONObject configuration supplied into HashMap
        HashMap<String, Object> props = JSONUtils.jsonToMap(jsonObject);
        initBot(props);

    }

    /**
     * {@inheritDoc} <br><br> The implementation in BaseBot introduces the concept of a an ExecutorService for
     * processing the incoming messages that are also buffered in a Kafka queue.  All Bots in the ecosystem need to
     * extend this BaseBot class. This is the primary mechanism for initializing a Bot with the ecosystem. This HashMap
     * contains the necessary information for initializing a specific Bot implementation.
     *
     * @throws Exception If the Bot cannot be initialized.
     */
    @Override
    public void initBot(HashMap<String, Object> props) throws Exception {

        // First you need to initialize the message bus
        botClassName = props.get(BotCommon.BOT_CLASSNAME).toString();
        HashMap<String, Object> mbmap = (HashMap<String, Object>) props.get(BotCommon.MESSAGE_BUS_CONFIG);

        //add bot uuid and bot type into mbmap to access these into KafkaMessageBusAdapter
        mbmap.put(BotCommon.BOT_TYPE, botClassName);
        mbmap.put(BotCommon.BOT_UUID, getUUID());

        botHeartBeatFrequency = (int) props.get(BotCommon.BOT_HEART_BEAT_FREQUENCY);
        consecutiveErrorLimit = (int) props.get(BotCommon.CONSECUTIVE_ERROR_LIMIT);
        //getting the topics list
        topics = (List<HashMap<String, Object>>) mbmap.get(BotCommon.PRIORITY_TOPIC_LIST);
        //read the thread count from the properties supplied
        int threadcount = (Integer) props.get(BotCommon.THREADPOOL_SIZE);

        //initialize the message bus
        initMessageBus(mbmap);

        //initialize the bot controller message bus
        initMessageBusController(mbmap);

        //Get bot controller config to publish status of bot
        //bcmap = (HashMap<String, Object>) props.get(BotCommon.BOT_CONTROLLER_CONFIG);

        //defines if pull is true for this Bot
        pull = Boolean.valueOf(MiscUtils.getProperty(mbmap, BotCommon.KAFKA_DELIVERY));
        //using Executors to span as thread
        executors = Executors.newFixedThreadPool(threadcount);
        logger
                .debug(new GregorianCalendar().toZonedDateTime().toString() + " :\t" + this.getClass().getCanonicalName() +
                        " is running");

    }

    /**
     * This implementation will call the fetchMessages() and process the messages sequentially when pull value is true
     */
    public void startBot() throws Exception {
        //if it is pull then start it as separate thread
        if (pull) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    fetchMessages();
                }
            }).start();
        }
        //commented to remove heart beat
        /* new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (shutdown == false) {
                        sendBotStatus();
                        Thread.sleep(botHeartBeatFrequency);
                    }
                }
                catch (MessageBusException e) {
                    e.printStackTrace();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

    /**
     * This implementation will shutdown the messageBusAdapter
     */
    @Override
    public void shutdown() throws Exception {
        logger.debug("Bot {} with uuid {} shutdown initialized ", botClassName, getUUID());
        //shutdown messagebus
        shutdown = true;
        if (messageBusAdapter != null && controllerMessageBusAdapter != null) {
            messageBusAdapter.shutdown();
            controllerMessageBusAdapter.shutdown();
        }
        //shutdown the thread executor
        if (executors != null) {
            executors.shutdown();
        }
    }

    /**
     * {@inheritDoc} <br><br> The implementation in BaseBot introduces the concept of buffering the incoming messages
     * and perform requested operations (PAUSE, RESUME, SHUTDOWN, STATUS)
     *
     * @param controlFunction The controlFunction message as bot controller operation
     */
    @Override
    public void controlMessageArrived(String controlFunction) throws Exception {
        logger.debug("Bot {} with uuid {} received control function {}", botClassName, getUUID(), controlFunction);
        //Check controlFunction is not null
        if (controlFunction != null) {
            switch (controlFunction) {
                case BotCommon.BOT_CONTROL_ACTION_PAUSE:
                    //controlFunction perform PAUSE action. Sets pause value to TRUE in messageBusAdapter
                    messageBusAdapter.pause();
                    botStatus = BotCommon.BOT_CONTROL_ACTION_PAUSE;
                    //sendBotStatus();
                    break;
                case BotCommon.BOT_CONTROL_ACTION_RESUME:
                    //controlFunction perform RESUME action. Sets pause value to FALSE in messageBusAdapter
                    messageBusAdapter.resume();
                    botStatus = BotCommon.BOT_STATUS_IDLE;
                    //sendBotStatus();
                    break;
                case BotCommon.BOT_CONTROL_ACTION_SHUTDOWN:
                    //controlFunction perform SHUTDOWN action. Calls shutdown() to set shutdown to TRUE
                    botStatus = BotCommon.BOT_CONTROL_ACTION_SHUTDOWN;
                    //sendBotStatus();
                    shutdown();
                    break;
                case BotCommon.BOT_CONTROL_ACTION_STATUS:
                    //controlFunction perform STATUS action. Calls sendBotStatus to send current status of BOT
                    //sendBotStatus();
                    break;
                default:
                    //Unknown control function exception
                    throw new MessageBusException(BotCommon.BOT_CONTROL_UNKNOWN_EXCEPTION);
            }
        }
        else {
            throw new MessageBusException(BotCommon.BOT_CONTROL_NULL_EXCEPTION);
        }
    }

    /**
     * This implementation in BaseBot publish the current bot status
     */
    synchronized private void sendBotStatus() throws MessageBusException {
        String outputMessage = null;
        try {
            //logger.debug("Bot Status " + botStatus);
            //Get bot control topic name
            String botControlTopic = (String) bcmap.get(BotCommon.TOPIC_LIST);
            //Get bot control outputTemplateGroup name
            String outputTemplateGroup = (String) bcmap.get(BotCommon.OUTPUT_TEMPLATE_GROUP);
            //Get bot control outputTemplateName
            String outputTemplateName = (String) bcmap.get(BotCommon.OUTPUT_TEMPLATE_NAME);

            //Declare outputMap to hold currentStatus, currentMessageMap and currentTaskMap
            Map<String, Object> outputMap = new HashMap<>();

            //Add currentStatus details to outputMap
            outputMap.put(BotCommon.BOT_CONTROL_CURRENT_STATUS, botStatus);
            outputMap.put(BotCommon.BOT_CONTROL_UUID, getUUID());
            //Add currentMessageMap details to outputMap
            if (currentMessageMap != null) {
                outputMap.put(BotCommon.BOT_CONTROL_CURRENT_MESSAGE_MAP, currentMessageMap);
            }
            else {
                outputMap.put(BotCommon.BOT_CONTROL_CURRENT_MESSAGE_MAP, BotCommon.BOT_CONTROL_NO_MESSAGE_MAP);
            }

            //Add currentTaskMap details to outputMap
            if (currentTaskMap != null) {
                outputMap.put(BotCommon.BOT_CONTROL_CURRENT_TASK_MAP, currentTaskMap);
            }
            else {
                outputMap.put(BotCommon.BOT_CONTROL_CURRENT_TASK_MAP, BotCommon.BOT_CONTROL_NO_TASK_MAP);
            }
            outputMap.put(BotCommon.BOT_CLASSNAME, botClassName);

            //Pass outputMap and get the output bot message from StringTemplateUtils
            outputMessage = StringTemplateUtils
                    .renderTemplate(outputTemplateGroup,
                            outputTemplateName, "data", new JSONObject(outputMap).toString());

            //publishing message to respective topic
            this.controllerMessageBusAdapter.publish(botControlTopic, outputMessage.getBytes("UTF-8"));
        }
        catch (Exception e) {
            logger.error(e.toString());
            logger.error("Failed Bot controller output message " + outputMessage);
        }
    }

    /**
     * This method which allows to publish a message given under byte array over the topic in the messaging
     * infrastructure.
     *
     * @param topic The topic which should be used when sending the message to the underlying message infrastructure.
     * @param mesg The message payload in the form of a byte array.  Note that if a binary payload is sent via this
     * publish call, it is expected that the payload will be Base64 encoded prior to sending.
     * @throws BotException when it cannot publish the message
     */
    @Override
    public void publish(String topic, byte[] mesg) throws BotException {
        try {
            //publish the message using the message bus registered
            messageBusAdapter.publish(topic, mesg);
        }
        catch (MessageBusException e) {
            logger.error(e.toString());
            throw new BotException(e);
        }
    }

    /**
     * Connects the Bot implementation with the adapter class that interacts with the underlying messaging
     * infrastructure.
     *
     * @param messageBusAdapter This is basically a client adapter that connects a Bot to the underlying messaging
     * infrastructure.
     * @throws MessageBusException then it cannot register the messageBus adaptor
     */
    @Override
    public void register(MessageBusAdapter messageBusAdapter) throws MessageBusException {
        this.messageBusAdapter = messageBusAdapter;
        this.messageBusAdapter.register(this);
    }

    /**
     * Connects the Bot controller implementation with the adapter class that interacts with the underlying messaging
     * infrastructure.
     *
     * @param controllerMessageBusAdapter This is basically a client adapter that connects a Bot to the underlying
     * messaging infrastructure.
     * @throws MessageBusException then it cannot register the messageBus controller adaptor
     */
    public void register(ControlMessageBusAdapter controllerMessageBusAdapter) throws MessageBusException {
        this.controllerMessageBusAdapter = controllerMessageBusAdapter;
        this.controllerMessageBusAdapter.register(this);
    }

    /**
     * This implementation in BaseBot configures the message bus for a given configuration
     *
     * @param mbmap This should be a map which holds the message bus configuration
     */
    public void initMessageBus(HashMap<String, Object> mbmap) throws Exception {
        //get the messge bus class and if it is not null
        if (mbmap != null && mbmap.containsKey(BotCommon.MSG_BUS_CLASSNAME)) {
            String messageBusClassName = (String) mbmap.get(BotCommon.MSG_BUS_CLASSNAME);
            try {
                //get the instance of the messagebus adapter
                messageBusAdapter = (MessageBusAdapter) Class.forName(messageBusClassName).newInstance();
                //initialize the message bus
                messageBusAdapter.initMessageBus(mbmap);
                //register the message bus adapter
                register(messageBusAdapter);
            }
            catch (Exception e) {
                throw new Exception("Unable to instantiate " + BotCommon.MSG_BUS_CLASSNAME);
            }
        }
    }

    /**
     * This implementation in BaseBot configures the message bus controller for a given configuration
     *
     * @param mbmap This should be a map which holds the message bus controller configuration
     */
    public void initMessageBusController(HashMap<String, Object> mbmap) throws Exception {
        //get the messge bus controller class and if it is not null
        if (mbmap != null && mbmap.containsKey(BotCommon.MSG_BUS_CLASSNAME_CONTROLLER)) {
            String messageBusClassName = (String) mbmap.get(BotCommon.MSG_BUS_CLASSNAME_CONTROLLER);
            try {
                //get the instance of the messagebus controller adapter
                controllerMessageBusAdapter = (ControlMessageBusAdapter) Class.forName(messageBusClassName)
                        .newInstance();
                //initialize the message bus controller
                controllerMessageBusAdapter.initMessageBus(mbmap);
                //register the message bus controller adapter
                register(controllerMessageBusAdapter);
            }
            catch (Exception e) {
                throw new Exception("Unable to instantiate " + BotCommon.MSG_BUS_CLASSNAME_CONTROLLER);
            }
        }
    }

    /**
     * {@inheritDoc} <br><br> The implementation in BaseBot introduces the concept of buffering the incoming messages in
     * a SynchonousQueue object for later processing the the ProcessMessages method.
     *
     * @param topic The underlying message system needs to provide a topic associated with the message payload.
     * @param mesg The payload of the message as a byte array.  Note that if the message
     */
    @Override
    public void messageArrived(String topic, byte[] mesg) throws MessageBusException {
        if (pull == false && shutdown == false) {
            String message = new String(mesg, StandardCharsets.UTF_8);
            //logger.debug("Message is :" + MiscUtils.formatString(message, true));
            //check if the message is well formed
            if (!MessageUtils.isMessageWellFormed(message)) {
                logger.error(BotCommon.PROBLEM_WITH_MESSAGE);
            }
            else {
                //process the message
                this.executors.execute(new Runnable() {
                    public void run() {
                        processMessage(message);
                    }
                });
            }
        }
    }

    /**
     * This implementation fetches a message, process the message and again fetch for new message when the pull is true
     * and shutdown is false
     */
    public void fetchMessages() {
        //fetch a message when pull is true and shutdown is false
        while (pull == true && shutdown == false) {
            try {
                if (shutdown != true) {
                    //fetch the message from message bus adapter
                    String message = messageBusAdapter.fetchMessage();
                    //logger.debug("Message is :" + MiscUtils.formatString(message, true));
                    //check for message well format if its valid in bot ecosystem
                    if (!MessageUtils.isMessageWellFormed(message)) {
                        logger.error(BotCommon.PROBLEM_WITH_MESSAGE);
                    }
                    else {
                        //process message
                        processMessage(message);
                    }
                }
            }
            catch (MessageBusException e) {
                try {
                    shutdown();
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * This implementation will process the received message and publish the processed output on respective topic from
     * the message
     *
     * @param message The message which needs to be processed is invoked by fetchMessage
     */
    @Override
    public void processMessage(String message) {
        botStatus = BotCommon.BOT_STATUS_ACTIVE;
        if (message != null) {
            HashMap<String, Object> messageMap = JSONUtils.jsonToMap(message);
            String messageUUID = MessageUtils.getUUID(messageMap);
            logger.debug("Bot {} with uuid {} started processing message with id {}", botClassName, getUUID(),
                    messageUUID);
            //initialize currentMessageMap to publish with bot controller status
            this.currentMessageMap = messageMap;
            boolean tasksDependency = MessageUtils.getTaskDependency(messageMap);
            ArrayList<HashMap<String, Object>> tasksMap = MessageUtils.getTasks(messageMap);

            // get all tasks in the message
            for (HashMap<String, Object> taskmap : tasksMap) {
                Long taskStartTimestamp = System.currentTimeMillis();
                try {

                    HashMap<String, Object> processorMap = MessageUtils.getProcessor(taskmap);
                    //initialize currentTaskMap to publish with bot controller status
                    this.currentTaskMap = taskmap;

                    HashMap<String, Object> appTemplateDetailsMap = MessageUtils
                            .getMap(taskmap, BotCommon.APP_TEMPLATE);

                    HashMap<String, Object> dataMap = MessageUtils.getTaskData(taskmap);

                    ArrayList<HashMap<String, Object>> outputTemplatesMap = MessageUtils
                            .getArrayList(taskmap, BotCommon.OUTPUT_TEMPLATES);

                    // apply processor for input data
                    HashMap<String, Object> processDataMapOutput = MessageUtils.processData(dataMap, processorMap);

                    //Implementation of data to  App_template block
                    String appTemplateOutput = StringTemplateUtils
                            .renderTemplate(MessageUtils.getString(appTemplateDetailsMap, BotCommon.APP_TEMPLATE_GROUP),
                                    MessageUtils.getString(appTemplateDetailsMap, BotCommon.APP_TEMPLATE_NAME), "data",
                                    processDataMapOutput);

                    logger.debug("AppTemplateOutput :\n" + appTemplateOutput != null ? MiscUtils
                            .formatString(appTemplateOutput, true) : "");

                    /*
                     *doNothingPattern is an attribute with value as "PATTERN"
                     * if the pattern mentioned in the doNothing equals to the app template output then the process logic
                     * of that particular task is skipped else the process logic is executed
                     */

                    String doNothingRegexPattern = MessageUtils.getString(taskmap, BotCommon.DO_NOTHING_PATTERN);

                    /*
                     * skipProcessBotLogic - true / false
                     * true - when the doNothingRegexPattern is not null and appTemplateOutput matches the appTemplateOutput
                     * false - when doNothingRegexPattern is null or  appTemplateOutput does not matches the appTemplateOutput
                     **/

                    boolean skipProcessBotLogic =
                            (doNothingRegexPattern != null) && (appTemplateOutput != null && appTemplateOutput
                                    .matches(doNothingRegexPattern));
                    String outputData = null;
                    // if skipProcessBotLogic is false then the processBotLogic will be executed
                    if (!skipProcessBotLogic) {
                        outputData = processBotLogic(dataMap, appTemplateOutput);
                    }

                    //Output Template provides the template to be published on outgoing topic
                    if (outputTemplatesMap != null) {
                        for (HashMap<String, Object> outputTemplateMap : outputTemplatesMap) {
                            /*
                             * useIncomingData is an attribute with value as true/false given along with message in the output template
                             * true - the incoming data is to be passed as an output data after processing the given task
                             * false - the processed app template output data will be sent as a data
                             * */
                            boolean useIncomingData =
                                    outputTemplateMap.get(BotCommon.USE_INCOMING_DATA_KEY) != null ? Boolean
                                            .valueOf(outputTemplateMap.get(BotCommon.USE_INCOMING_DATA_KEY).toString()) : false;
                            // if the useIncomingData is false and skipProcessBotLogic is true then no action is taken i.e, output message will not be sent
                            if (useIncomingData == false && skipProcessBotLogic == true) {
                                logger.debug("Skipping as useIncomingData == false && skipProcessBotLogic == true");
                            }
                            else {
                                String outputTemplateData =
                                        useIncomingData ? JSONUtils.object2JsonString(dataMap) : outputData;

                                Object dataObj = useIncomingData ? dataMap : JSONUtils.jsonStringToObject(outputData);

                                String outputMessage = MessageUtils
                                        .renderMessage(
                                                MessageUtils.getString(outputTemplateMap, BotCommon.OUTPUT_TEMPLATE_GROUP),
                                                MessageUtils.getString(outputTemplateMap, BotCommon.OUTPUT_TEMPLATE_NAME),
                                                outputTemplateData,dataObj ,messageMap,
                                                taskmap, getUUID(), taskStartTimestamp);

                                logger.debug("outputMessage\n {} ", MiscUtils.formatString(outputMessage, true));

                                HashMap<String, Object> outputMessageMap = JSONUtils.jsonToMap(outputMessage);

                                String publish_msg_topic = MessageUtils.getTopic(outputMessageMap);

                                //publishing message to respective topic
                                publish(publish_msg_topic, outputMessage.getBytes(StandardCharsets.UTF_8));
                            }
                        }
                    }
                    //if task is completed successfully, after any exception occurrence in previous attempt then error count need to reset to Zero.
                    errorCount = 0;
                }
                catch (Exception e) {
                    errorCount++;
                    logger.debug("Bot : {} with uuid : {} error count : {}", botClassName, getUUID(), errorCount);
                    logger.error("Exception Message :", e);
                    //apply error template
                    logErrorMessage(e, messageMap, taskmap, taskStartTimestamp);
                    if (tasksDependency) { // break for loop if tasks are dependent on each other
                        break;
                    }
                }
                finally {
                    //Change bot status as IDLE if botstatus is not pause
                    if (botStatus != BotCommon.BOT_CONTROL_ACTION_PAUSE) {
                        botStatus = BotCommon.BOT_STATUS_IDLE;
                    }
                    if (errorCount >= consecutiveErrorLimit) {
                        try {
                            messageBusAdapter.pause();
                            botStatus = BotCommon.BOT_CONTROL_ACTION_PAUSE;
                            //sendBotStatus();
                            //if Bot is pause , after exceeding consecutive exception occurance then error count need to reset to Zero again.
                            errorCount = 0;
                            logger.debug("Bot : {} with uuid : {} has paused and errorCounter has been reset to 0",
                                    botClassName, getUUID());
                        }
                        catch (Exception e) {
                            logger.error("Bot " + getUUID() + " failed to pause", e);
                        }
                    }
                }
            }
            this.currentTaskMap = null;
            this.currentMessageMap = null;
            logger.debug("Bot {} with uuid {} finished processing message with id {}", botClassName, getUUID(),
                    messageUUID);
        }
    }

    /**
     * The processBotLogic implementation will be different for each bot to process its own logic
     *
     * @param dataMap This should be a HashMap which hold the data required to process the Bot logic
     * @param appTemplateOutput The String template output obtained by applying the data to appTemplate
     * @return The output as string format after processing the Bot
     */
    protected abstract String processBotLogic(HashMap<String, Object> dataMap, String appTemplateOutput)
            throws Exception;

    /**
     * This method returns UUID of the Bot
     *
     * @return uuid of the bot
     */
    public String getUUID() {
        return UUIDString;
    }

    /**
     * It is used to publish error message to error bot
     *
     * @param e as Exception
     * @param messageMap input bot message
     * @param taskmap Hashmap of task details
     * @param taskStartTimestamp timestamp at which task has started
     */
    private void logErrorMessage(Exception e, HashMap messageMap, HashMap taskmap, Long taskStartTimestamp) {
        try {

            //getting stack trace of the error occured
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String stackTrace = writer.toString();

            //preparing the error map
            HashMap<String, Object> errorMap = new HashMap<>();
            errorMap.put(BotCommon.ERROR_TYPE, e.getClass().getCanonicalName().toString());
            errorMap.put(BotCommon.ERROR_DESCRIPTION, e.fillInStackTrace());
            errorMap.put(BotCommon.ERROR_STACKTRACE, stackTrace);

            //preparing the error output message map
            HashMap<String, Object> errorOutputMessageMap = new HashMap<>();
            errorOutputMessageMap.put(BotCommon.ERROR_JSON, new JSONObject(errorMap).toString());
            errorOutputMessageMap.put(BotCommon.TASK_MAP, new JSONObject(taskmap).toString());

            //Get the error template
            ArrayList<HashMap<String, Object>> errorTemplateDetailsList = MessageUtils
                    .getArrayList(taskmap, BotCommon.ERROR_TEMPLATES);
            if (errorTemplateDetailsList != null && errorTemplateDetailsList.size() > 0) {
                for (HashMap<String, Object> errorTemplateDetailsMap : errorTemplateDetailsList) {
                    //render the error template output with error information present in error output map
                    String errorTemplateOutput = MessageUtils
                            .renderMessage(MessageUtils.getString(errorTemplateDetailsMap, BotCommon.ERROR_TEMPLATE_GROUP),
                                    MessageUtils.getString(errorTemplateDetailsMap, BotCommon.ERROR_TEMPLATE_NAME),
                                    new JSONObject(errorOutputMessageMap).toString(),errorOutputMessageMap,
                                    messageMap, taskmap, getUUID(), taskStartTimestamp);

                    logger.debug("Error outputMessage :\n" + errorTemplateOutput != null ? MiscUtils
                            .formatString(errorTemplateOutput, true) : "");

                    HashMap<String, Object> errorMessageMap = JSONUtils.jsonToMap(errorTemplateOutput);
                    //get the topic to publish this error
                    String publish_msg_topic = MessageUtils.getTopic(errorMessageMap);
                    try {
                        //publishing message to ErrorBot
                        publish(publish_msg_topic, errorTemplateOutput.getBytes(StandardCharsets.UTF_8));
                    }
                    catch (Exception e2) {
                        e2.printStackTrace();
                        logger.error("Exception while publishing error message", e2);
                    }
                }
            }
        }
        catch (Exception e1) {
            e1.printStackTrace();
            logger.error("Exception in logErrorMessage() ", e1);
        }
    }
}