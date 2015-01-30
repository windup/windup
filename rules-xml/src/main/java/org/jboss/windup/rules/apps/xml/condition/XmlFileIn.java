package org.jboss.windup.rules.apps.xml.condition;

import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * The next step in XmlFile condition definition.
 */
public interface XmlFileIn  extends ConditionBuilder
{

    /**
     * Specify the namespace used in the xpath that was provided sooner.
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
