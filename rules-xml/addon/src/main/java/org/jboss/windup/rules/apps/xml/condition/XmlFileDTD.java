package org.jboss.windup.rules.apps.xml.condition;

import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * The next step in building XmlFile condition after DTD was specified.
 * @author mbriskar
 *
 */
public interface XmlFileDTD extends ConditionBuilder
{
    /**
     * Specify the regex of xml file name. 
     * @param fileName regex of file name
     * @return
     */
    XmlFileIn inFile(String fileName);
    
    /**
     * Specify the namespace used in the xpath that was provided sooner. Use this only if you don't want to specify file regex of the file.
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
    public ConditionBuilder as(String variable);
}
