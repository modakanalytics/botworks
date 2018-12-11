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

package com.modak.utils;

import org.apache.logging.log4j.LogManager;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.StringRenderer;

import java.util.HashMap;

/**
 * This class is used to render the templates
 *
 * @author modakanalytics
 * @version 1.0
 * @since 2017-11-02
 */
public class StringTemplateUtils {

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

    /**
     * This method returns the string after rendering the template
     *
     * @param templateGroupName To which group the template belongs to
     * @param templateName The name that is to be used in the template
     * @param attributeName The attribute to which the data needs to be sent
     * @param inputMap The data to be sent
     * @return returns string after rendering the template
     */
    public static String renderTemplate(String templateGroupName, String templateName, String attributeName,
        Object inputMap) {
        STGroupFile stfile = new STGroupFile(templateGroupName, '$', '$');
        stfile.registerRenderer(String.class, new StringRenderer());
        ST temp = stfile.getInstanceOf(templateName);
        if (temp == null) {
            throw new NullPointerException("Template " + templateName + " is not defined in " + stfile.getFileName());
        }
        temp.add(attributeName, inputMap);
        String retval = temp.render();
        return retval;
    }

    /**
     * This method returns the string after rendering the template
     *
     * @param templateGroupName To which group the template belongs to
     * @param templateName The name that is to be used in the template
     * @param templateInputs The data to be sent for the required template name
     * @return returns string after rendering the template
     */
    public static String renderTemplate(String templateGroupName, String templateName,
        HashMap<String, Object> templateInputs) {
        STGroupFile stfile = new STGroupFile(templateGroupName, '$', '$');
        stfile.registerRenderer(String.class, new StringRenderer());
        ST temp = stfile.getInstanceOf(templateName);
        if (temp == null) {
            throw new NullPointerException("Template " + templateName + " is not defined in " + stfile.getFileName());
        }
        for (String attributeName : templateInputs.keySet()) {
            temp.add(attributeName, templateInputs.get(attributeName));
        }
        String retval = temp.render();
        return retval;
    }

}
