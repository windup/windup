package org.jboss.windup.rules.apps.xml.condition;

import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * The building step of XmlFile condition
 * @author mbriskar
 *
 */
public interface XmlFileXpath extends ConditionBuilder
{

    /**
     * Specify the regex to which should the xpath result be matched.
     * @param regex that should the xpath result match
     * @return
     */
    XmlFileResult resultMatches(String regex);
    
    /**
     * Specify the DTD public-id regex. Use this only if you don't have the regex for the xpath result.
     * @param publicIdRegex public-id regex
     * @return
     */
    XmlFileDTD andDTDPublicId(String publicIdRegex);
    
    /**
     * Specify the regex of xml file name. Use this now only if you don't want to specify regex for the xpath result nor DTD regex.
     * @param fileName regex of file name
     * @return
     */
    XmlFileIn inFile(String fileName);
    
    /**
     * Specify the namespace used in the xpath that was provided sooner. Use this now only if you don't want to specify any of these: xpath result regex, DTD regex, file naem regex.
     * @param prefix the namespace prefix
     * @param url the namespace url
     * @return the last step in XmlFile definition to provide the variable name
     */
    XmlFileNamespace namespace(String prefix, String url);
    
    /**
     * The last step of building the XmlFile, specifying the variable name. Do not use this now if you want to specify some other parameters.
     * @param variable variable name that will be saved in {@Variables} stack as the output of this condition.
     * @return
     */
    ConditionBuilder as(String variable);
}
