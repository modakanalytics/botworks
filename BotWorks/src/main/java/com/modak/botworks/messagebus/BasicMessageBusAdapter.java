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

import com.modak.utils.*;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import java.util.HashMap;

/**
 * This class implements a couple of convenience items for initializing the MessageBus connection.
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-06-13
 */

public abstract class BasicMessageBusAdapter implements MessageBusAdapter {

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

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
}
