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
package com.modak.botworks.messagebus;

import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;

/**
 * This interface defines a generic messaging framework using Pub/Sub type model under Bots eco system.
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2018-02-12
 */

public interface ControlMessageBusAdapter {

    /**
     * Subscribe to the underlying messaging infrastructure with a specified Quality of Service.
     *
     * @param topics Specifies all the topics that the client module is listening.
     * @param QualityOfService The meaning of this parameter will vary with the  underlying messaging infrastructure
     * @throws MessageBusException If a subscription cannot be completed, then implementation needs to throw this
     * exception.
     */
    public void subscribe(List<String> topics, int QualityOfService) throws MessageBusException;

    /**
     * Subscribe to the underlying messaging infrastructure with the default Quality of Service
     *
     * @param topics Specifies all the topics that the client module is listening.
     * @throws MessageBusException If a subscription cannot be completed, then implementation needs to throw this
     * exception.
     */
    public void subscribe(List<String> topics) throws MessageBusException;

    /**
     * @param topic The topic to use when publishing this message.
     * @param mesg The message payload.  If it contains binary type data it is expected that you Base64 encode the
     * data.
     * @throws MessageBusException If the adapter is unable to publish, then it should throw this exception.
     */
    public void publish(String topic, byte[] mesg) throws MessageBusException;

    /**
     * @param topic The topic to use when publishing this message.
     * @param mesg The message payload.  If it contains binary type data it is expected that you Base64 encode the
     * data.
     * @param QualityOfService The meaning of this parameter will vary with the  underlying messaging infrastructure
     * @throws MessageBusException If the adapter is unable to publish, then it should throw this exception.
     */
    public void publish(String topic, byte[] mesg, int QualityOfService) throws MessageBusException;

    /**
     * This allows the client to register and potentially autenticate so that it can send and receive messages to/from
     * the underlying infrastructure
     *
     * @throws MessageBusException f the adapter is unable to register, then it should throw this exception.
     */
    public void register(BotControlMessageChannel botControlMessageChannel) throws MessageBusException;

    /**
     * Removes the subscription for one or more topics.
     *
     * @param topics A list of topics that should removed from active subscriptions
     * @throws MessageBusException If unable to perform the task, then it should throw this exception.
     */
    public void unsubscribe(List<String> topics) throws MessageBusException;

    /**
     * Provides any infrastructure specific configuration that is needed to provide the services of the underlying
     * messaging system.
     *
     * @param configuration A JSON formatted document that contains implementation specific configuration information.
     * @throws MessageBusException If unable to initialize, then this Exception should be thrown.
     */
    public void initMessageBus(String configuration) throws MessageBusException;

    /**
     * Provides any infrastructure specific configuration that is needed to provide the services of the underlying
     * messaging system.
     *
     * @param jsonObject A JSON object that contains implementation specific configuration information.
     * @throws MessageBusException If unable to initialize, then this Exception should be thrown.
     */
    public void initMessageBus(JSONObject jsonObject) throws MessageBusException;

    /**
     * Provides any infrastructure specific configuration that is needed to provide the services of the underlying
     * messaging system.
     *
     * @param props A set of key-value pairs that contain infrastructure specific information.
     * @throws MessageBusException If unable to initialize, then this Exception should be thrown.
     */
    public void initMessageBus(HashMap<String, Object> props) throws MessageBusException;

    /**
     * This should terminate the connection with the underlying messaging infrastructure and cleans up any resources
     * used by the adapter.
     *
     * @throws Exception If unable to terminate, then an Exception is thrown.
     */
    public void shutdown() throws Exception;
}
