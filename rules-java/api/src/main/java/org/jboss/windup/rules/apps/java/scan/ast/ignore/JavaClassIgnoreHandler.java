package org.jboss.windup.rules.apps.java.scan.ast.ignore;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

/**
 * <javaclass-ignore> is used to mark the packages/classes/methods that once are referenced, the class should be ignored.
 * This is especially useful for cases in which just by some reference we may for sure tell that the class is generated.
 * <p>
 * Note: Inner classes are still kept in the analysis.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@NamespaceElementHandler(elementName = JavaClassIgnoreHandler.ELEMENT_NAME, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class JavaClassIgnoreHandler implements ElementHandler<Void> {
    public static final String ELEMENT_NAME = "javaclass-ignore";
    private static final String REFERENCE_PREFIX = "reference-prefix";

    @Override
    public Void processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String referencePrefix = $(element).attr(REFERENCE_PREFIX);
        //just register it in a singleton
        JavaClassIgnoreResolver.singletonInstance().addInterest(referencePrefix);
        return null;
    }
}

