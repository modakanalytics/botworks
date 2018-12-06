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

/**
 * All Bots need to implement the BotControlMessageChannel interface.  This assumes that the underlying messaging system
 * conforms to a Pub/Sub type of model like MQTT messaging.
 *
 *  @author modakanalytics
 * @version 1.0
 * @since 2018-02-05
 */

public interface BotControlMessageChannel {

    /**
     * The underlying messaging system should have a mechanism for executing this callback method.
     *
     * @param controlMessage The underlying message system needs to provide a topic associated with the message
     *                       payload.
     */
    public void controlMessageArrived(String controlMessage) throws Exception;


    /**
     *
     * @return The unique UUID assigned to the BOT. This is used to filter the messages that are specific for the BOT.
     * @throws Exception
     */
    public String getUUID()  ;
}
