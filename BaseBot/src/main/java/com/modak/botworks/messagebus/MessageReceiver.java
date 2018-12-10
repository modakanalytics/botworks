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
 * All Bots need to implement the MessageReciever interface.  This assumes that the underlying messaging system conforms
 * to a Pub/Sub type of model like Kafka messaging.
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-02
 */

public interface MessageReceiver {

    /**
     * The underlying messaging system should have a mechanism for executing this callback method.
     *
     * @param topic The underlying message system needs to provide a topic associated with the message payload.
     * @param mesg The payload of the message as a byte array.  Note that if the message contains binary data the
     * expectation is that it will be Base64 encoded.
     */
    public void messageArrived(String topic, byte[] mesg) throws MessageBusException;

}
