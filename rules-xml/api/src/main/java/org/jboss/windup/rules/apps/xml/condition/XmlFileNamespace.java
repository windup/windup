package org.jboss.windup.rules.apps.xml.condition;

import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * The last step of building the XmlFile condition after the namespace was specified.
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 *
 */
public interface XmlFileNamespace extends ConditionBuilder
{
    
    /**
     * The last step of building the XmlFile, specifying the variable name.
     * @param variable variable name that will be saved in {@Variables} stack as the output of this condition.
     * @return
     */
    ConditionBuilder as(String variable);
}
