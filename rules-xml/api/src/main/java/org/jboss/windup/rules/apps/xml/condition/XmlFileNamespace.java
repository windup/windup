package org.jboss.windup.rules.apps.xml.condition;

import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * The last step of building the XmlFile condition after the namespace was specified.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public interface XmlFileNamespace extends ConditionBuilder {

    /**
     * The last step of building the XmlFile, specifying the variable name.
     *
     * @param variable variable name that will be saved in {@Variables} stack as the output of this condition.
     * @return
     */
    ConditionBuilder as(String variable);

    /**
     * Specify the namespace used in the xpath that was provided sooner. Use this now only if you don't want to specify any of these: xpath result regex, DTD regex, file naem regex.
     *
     * @param prefix the namespace prefix
     * @param url    the namespace url
     * @return the last step in XmlFile definition to provide the variable name
     */
    XmlFileNamespace namespace(String prefix, String url);
}
