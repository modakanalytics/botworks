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
 * An specialized Exception class that relates specifically the MessageBus issues.
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-02
 */

public class MessageBusException extends Exception {

    /**
     * Just pass any message to the Exception class.
     *
     * @param mesg The message associated with the Exception
     */
    public MessageBusException(String mesg) {
        super(mesg);
    }

    /**
     * Just pass the Throwable object to the Exception class.
     *
     * @param mesg A Throwable (Exception) object.
     */
    public MessageBusException(Throwable mesg) {
        super(mesg);
    }
}
