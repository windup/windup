package org.jboss.windup.rules.apps.java.scan.ast;

import static org.joox.JOOX.$;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = TypeInterestHandler.ELEMENT_NAME, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class TypeInterestHandler implements ElementHandler<Void>
{
    public static final String ELEMENT_NAME = "type-interest";
    private static final String PACKAGE_PREFIX = "package";
    private static final String CLASSNAME = "class";
    private static final String METHOD = "method";

    @Override
    public Void processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String packagePrefix = $(element).attr(PACKAGE_PREFIX);
        String className = $(element).attr(CLASSNAME);
        String methodName = $(element).attr(METHOD);

        TypeInterest typeInterest = new TypeInterest(packagePrefix, className, methodName);
        TypeInterestResolver.defaultInstance().addTypeInterest(typeInterest);

        return null;
    }
}
