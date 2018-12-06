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

import com.modak.botworks.messagebus.MessageBusAdapter;
import com.modak.botworks.messagebus.MessageBusException;
import com.modak.botworks.messagebus.MessageReceiver;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Bot interface extends MessageReceiver interface. This interface defines the methods that all bots must implement.
 * Bots are independent execution units that communicate using topic based messaging. A Bot receiving a message need not
 * have knowledge of the origin of the message nor does it need to know anything about the implementation or entities
 * that receive the message(s) that it emits upon completion of work.
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-15
 */

public interface Bot extends MessageReceiver {

    /**
     * This method which allows to publish a message given under byte array over the topic in the messaging
     * infrastructure.
     *
     * @param topic The topic which should be used when sending the message to the underlying message infrastructure.
     * @param mesg The message payload in the form of a byte array.  Note that if a binary payload is sent via this
     * publish call, it is expected that the payload will be Base64 encoded prior to sending.
     * @throws BotException In the event that
     */
    public void publish(String topic, byte[] mesg) throws BotException;

    /**
     * Connects the Bot implementation with the adapter class that interacts with the underlying messaging
     * infrastructure.
     *
     * @param messageBusAdapter This is basically a client adapter that connects a Bot to the underlying messaging
     * infrastructure.
     * @throws MessageBusException If it is not possible to connect to the underlying infrastructure.
     */
    public void register(MessageBusAdapter messageBusAdapter) throws MessageBusException;

    /**
     * A convenience method to allow the Bot to be initialized using a JSON formatted String object.
     *
     * @param configuration This should be a valid JSON document that contains the parameters and information needed by
     * a specific Bot implementation in order to configure and register the Bot with the ecosystem.
     * @throws Exception If the Bot cannot be initialized.
     */
    public void initBot(String configuration) throws Exception;

    /**
     * A convenience method to allow the Bot to be initialized using a JSON  object.
     *
     * @param configuration This should be a valid JSON Object that contains the parameters and information needed by a
     * specific Bot implementation in order to configure and register the Bot with the ecosystem.
     * @throws Exception If the Bot cannot be initialized.
     */
    public void initBot(JSONObject configuration) throws Exception;

    /**
     * This is the primary mechanism for initializing a Bot with the ecosystem.
     *
     * @param configuration This HashMap contains the necessary information for initializing a specific Bot
     * implementation.
     * @throws Exception If the Bot cannot be initialized.
     */
    public void initBot(HashMap<String, Object> configuration) throws Exception;

    /**
     * Bots need to expect messages that could arrive at any time during their lifespan.  These messages are expected to
     * be JSON formatted in accordance with the ecosystem specification.  Only the data section of the message is an
     * area that may be fully customized for the specific Bot implementation.
     *
     * @param message in the form of JSON String.
     */
    public void processMessage(String message);

    /**
     * The implementation of the Bot should cleanup all resources and terminate any task execution that it has
     * underway.
     *
     * @throws Exception Throw an exception if unable to shutdown and cleanup resources.
     */
    public void shutdown() throws Exception;

    /**
     * Get the UUID specific to this Bot instance
     *
     * @return String which has UUID information of the Bot instance
     */
    public String getUUID();

    /**
     * This implementation of code should start a bot and start fetching a message
     *
     * @throws Exception Throw an exception if unable to start a bot.
     */
    public void startBot() throws Exception;
}
