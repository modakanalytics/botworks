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

package com.modak.botworks.bots.processor;

import com.modak.botworks.bots.BotException;
import com.modak.utils.*;

import java.util.HashMap;

/**
 * This class returns the datamap as it is so that it can be applied apptemplate
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-05-31
 */

public class NullProcessor implements MessageProcessor {

    /**
     * This method modifies or enhances the data supplied as String by considering the entries supplied in the config.
     * The enhancement is defined in the implementation - in this method implementation it does nothing but just to pass
     * the Bot rules of having a preprocessor stage
     *
     * @param data This should be a JSON formatted string containing key-value pairs suitable for conversion into a
     * HashMap.  Note that the values can be objects such as lists, etc.
     * @param config This should be a JSON formatted string that contains information needed for the processing of the
     * data.
     */
    @Override
    public HashMap<String, Object> process(String data, String config) throws BotException {
        return process(JSONUtils.jsonToMap(data), JSONUtils.jsonToMap(config));
    }

    /**
     * This method modifies or enhances the data supplied as Map object by considering the entries supplied in the
     * config props Map. The enhancement is defined in the implementation - in this method implementation it does
     * nothing but just to pass the Bot rules of having a preprocessor stage
     *
     * @param data Contains the data related to a Bot message that can be enhanced, modified, etc.
     * @param props Contains any properties needed for the
     */
    @Override
    public HashMap<String, Object> process(HashMap<String, Object> data, HashMap<String, Object> props)
        throws BotException {
        return data;
    }
}
