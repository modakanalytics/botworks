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

package com.modak.botworks.bots.common;

/**
 * This class has all the string constants used in the Bot specific module
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-15
 */

public class BotCommon {

    public static final String BOT_CLASSNAME = "bot_classname";
    public static final String MSG_BUS_CLASSNAME = "messagebus_classname";
    public static final String MESSAGE_BUS_CONFIG = "messagebus_config";
    public static final String TOPIC_LIST = "topics";
    public static final String PRIORITY_TOPIC_LIST = "priority_topics";
    public static final String THREADPOOL_SIZE = "thread_pool_size";
    public static final String PARENT_METADATA = "parent_metadata";
    public static final String METADATA = "metadata";
    public static final String PARENT_TASK_NAME = "parent_task_name";
    public static final String PARENT_TASK_START_TS = "parent_task_start_ts";
    public static final String PARENT_TASK_END_TS = "parent_task_end_ts";
    public static final String USE_INCOMING_DATA_KEY = "useIncomingData";
    public static final String OUTPUT_TEMPLATES = "output_templates";
    public static final String OUTPUT_TEMPLATE_GROUP = "output_template_group";
    public static final String OUTPUT_TEMPLATE_NAME = "output_template_name";
    public static final String APP_TEMPLATE = "app_template";
    public static final String APP_TEMPLATE_GROUP = "app_template_group";
    public static final String APP_TEMPLATE_NAME = "app_template_name";
    public static final String DATA = "data";
    public static final String DATA_OBJ = "dataObj";
    public static final String MESSAGE_UUID = "message_uuid";
    public static final String BOT_UUID = "bot_uuid";
    public static final String KAFKA_DELIVERY = "pull";
    public static final String KAFKA_BOOTSTRAP_SERVERS = "bootstrap_servers";
    public static final String KAFKA_CLIENT_ID = "clientid";
    public static final String DO_NOTHING_PATTERN = "doNothingPattern";
    public static final String ERROR_TEMPLATES = "error_templates";
    public static final String ERROR_TEMPLATE_GROUP = "error_template_group";
    public static final String ERROR_TEMPLATE_NAME = "error_template_name";
    public static final String ERROR_TYPE = "error_type";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String ERROR_STACKTRACE = "error_stackTrace";
    public static final String ERROR_JSON = "error_json";
    public static final String TASK_MAP = "task_map";
    public static final String TASK_NAME = "name";
    public static final String PROBLEM_WITH_MESSAGE = "Message is either malformed or doesn't follow Bot Message standards";
    public static final String MSG_BUS_CLASSNAME_CONTROLLER = "messagebus_classname_controller";
    public static final String BOT_CONTROLLER_CONFIG = "bot_controller_config";
    public static final String BOT_STATUS_IDLE = "IDLE";
    public static final String BOT_STATUS_ACTIVE = "ACTIVE";
    public static final String BOT_CONTROL_GROUP_ID = "group_id";
    public static final String BOT_CONTROL_ACTION_PAUSE = "PAUSE";
    public static final String BOT_CONTROL_ACTION_RESUME = "RESUME";
    public static final String BOT_CONTROL_ACTION_SHUTDOWN = "SHUTDOWN";
    public static final String BOT_CONTROL_ACTION_STATUS = "STATUS";
    public static final String BOT_CONTROL_SWITCH = "bot_control_switch";
    public static final String BOT_CONTROL_UNKNOWN_EXCEPTION = "Unknown control function";
    public static final String BOT_CONTROL_NULL_EXCEPTION = "Control function is null";
    public static final String BOT_CONTROL_CURRENT_STATUS = "currentStatus";
    public static final String BOT_CONTROL_CURRENT_MESSAGE_MAP = "currentMessageMap";
    public static final String BOT_CONTROL_CURRENT_TASK_MAP = "currentTaskMap";
    public static final String BOT_CONTROL_NO_MESSAGE_MAP = "NoMessageRunning";
    public static final String BOT_CONTROL_NO_TASK_MAP = "NoTaskRunning";
    public static final String BOT_CONTROL_UUID = "uuid";
    public static final String BOT_HEART_BEAT_FREQUENCY = "bot_heart_beat_frequency_ms";
    public static final String CONSECUTIVE_ERROR_LIMIT = "consecutive_error_limit";
    public static final String PARENT_BOT_TYPE = "parent_bot_type";
    public static final String BOT_TYPE = "bot_type";
    public static final String PRIORITY = "priority";
    public static final String PUBLIC_KEY = "public_key";
    public static final String PRIVATE_KEY = "private_key";

    public static final String BOT_PUBLISH_MESSAGE_SIZE = "bot_publish_message_size";
    public static final String BOT_PUBLISH_MESSAGE_SIZE_EXCEPTION = "Publish message size exceeded 16MB, OutputMessageSize : ";
    public static final String BOT_PUBLISH_MESSAGE_SIZE_BYTES = "Message in Bytes : ";


    public static final String CONTROL_TOPIC = "control_topic";
    public static final String CONTROL_TOPIC_GROUP_PREFIX = "controlgroup-";

    //botworks commons
    public static final String LOG4J_CONFIG_FILE_WITH_PATH = "log4j_config_file_with_path";
    public static final String BOTFACTORY_CONFIG_FILE= "factory_bot_config_file_with_path";
    public static final String PRODUCER_CONFIG_FILE_PATH = "producer_config_file_with_path";

    //factorybot commons
    public static final String FACTORY_BOT_CONFIG = "config";
    public static final String NO_OF_INSTANCES = "no_of_instances";
    public static final String BOTS_LIST = "bots_list";
    public static final String EXCEPTION_OCCURED_STARTING_BOT = "Exception occured when starting a new bot";
    public static final String FAILED_START_BOT_CONFIG = "Failed to start bot with config";
    public static final String FAILED_TO_START_BOT = "Failed to start bot";
    public static final String STARTED_A_NEW_BOT = "Started a new bot";
    public static final String INVALID_INPUT_CONFIGURATIONS = "Invalid input configurations";
    public static final String UUID = "uuid";
    public static final String DATAMAP= "DataMap : ";
    public static final String BOTSCONFIG_IS_NULL_OR_PROCESSID = "BotsConfig is null or processid is 0";

    //firemesagetobot commons
    public static final String LOG_MSG_ARGS_ERROR = "MessageGenerator Utility is not supplied with configurations directory path. \n So Couldn't fire a bot message !" ;
    public static final String LOGGER_CONFIG_FILE_IS_FOUND_INFO  = "logger config file is  found : {}";
    public static final String LOGGER_CONFIG_FILE_LOG4J2_XML_IS_NOT_FOUND_INFO = "logger config file i.e log4j2.xml is not found! : {}";
    public static final String TOPIC_CONFIG_ATTRIBUTE = "topic";
    public static final String MESSAGE_PATH_ATTRIBUTE = "bot_message_path";
    public static final String PUBLICKEY_PATH = "public_key_path";

    //jdbcbot commons
    public static final String DATABASE_CONFIG = "database_config";
    public static final String INVALID_ARG = "Invalid object passed to initBot call";
    public static final String QUERY_NULL = "Query is null";
    public static final String QUERY_TYPE = "query_type";
    public static final String QUERY_TYPE_NULL = "QueryType is null";
    public static final String AFFECTED_ROWS_NUMBER = "AffectedRowsNumber";
    public static final String QUERY_EXECUTION_ERROR = "Failed to execute jdbc query ";
    public static final String QUERY_TYPE_INVALID = "Invalid query type";
    public static final String QUERY_TYPE_SELECT = "select";
    public static final String QUERY_TYPE_UPDATE = "update";
    public static final String QUERY_TYPE_DELETE = "delete";
    public static final String QUERY_TYPE_CREATE = "create";
    public static final String QUERY_TYPE_INSERT = "insert";

    //spawn bot commons
    public static final String BOOTSTRAP_SERVERS_CONFIG = "bootstrap_servers_config";
    public static final String KEY_SERIALIZER_CLASS_CONFIG = "key_serializer_class_config";
    public static final String VALUE_SERIALIZER_CLASS_CONFIG = "value_serializer_class_config";
    public static final String ACKS_CONFIG = "acks_config";
    public static final String PUBLIC_KEY_PATH = "public_key_path";
    public static final String BOT_FACTORY_TOPICS = "bot_factory_topics";
    public static final String TEMPLATE_GROUP = "template_group";
    public static final String TEMPLATE_NAME = "template_name";
    public static final String PROCESS_ID = "process_id";
    public static final String MSG_UUID = "msg_uuid";
    public static final String MESSAGE_PUBLISHED = "Message Published : {}";
    public static final String PROCESS_ID_PUBLISHED_ON_TOPIC_MESSAGE_SIZE_INFO = "Process id : {}, Published on - Topic : {}, Message size : {} ";

    //utils commons
    public static final String JDBC_DRIVER = "jdbc_driver";
    public static final String JDBC_URL = "jdbc_url";
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String PRIVATE_KEY_FILE_PATH = "private_key_file_path";
    public static final String ENCRYPTED_PASSWORD = "encrypted_password";
    public static final String YES = "Y";
    public static final String MIN_CONNECTION_PER_PARTITION = "minConnectionsPerPartition";
    public static final String MAX_CONNECTION_PER_PARTITION = "maxConnectionsPerPartition";
    public static final String LEAK_DETECTION_THRESHOLD = "leakDetectionThreshold";


    //ondemandjdbcbot commons
    public static final String OUTPUT_NULL = "Template Processed output in null";
    public static final String ONDEMAND_CONFIG = "ondemand_config";
    public static final String EXECUTION_ERROR = "Failed while initializing pool or executing the query or returning the output list of map";
    public static final String EXECUTION_DESC = "Error occured due to :";
    public static final String INVALID_INPUT_DATAMAP = "Datamap is null or missing ondemand config";

    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String DATA_LIST = "dataList";
    public static final String STATUS = "status";
    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";

    //message generator commons

    public static final String BOT_MESSAGE_TEMPLATE_GROUP = "bot_message_template_group";
    public static final String BOT_MESSAGE_TEMPLATE_NAME = "bot_message_template_name";
    public static final String INFORMATION_OF_SOURCE_TO_STREAM = "information_of_source_to_stream";
    public static final String BOT_INITIAL_MESSAGE_PUBLISHED = "Bot initial message published";

    //message listener commons
    public static final String MESSAGE_LISTENER_LOG_MSG_ARGS_ERROR = "MessageListener Utility is not supplied with configurations directory path. \n So Couldn't fire a bot message !" ;
    public static final String PROCESS_CONTEXT = "metadata.process_context";
    public static final String LOG_MESSAGE = "Bot : {} started | Task : {} at {}";
    public static final String TIME_PATTERN = "MMM dd,yyyy HH:mm:ss";
    public static final String CONSUMER_CONFIG_FILE_PATH="consumer_config_file_with_path";
    public static final String MESSAGE_LISTENER_CONFIG_FILE_WITH_PATH = "message_listener_config_file_with_path";
}
