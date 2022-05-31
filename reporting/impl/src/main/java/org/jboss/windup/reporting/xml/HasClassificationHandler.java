package org.jboss.windup.reporting.xml;

import org.jboss.forge.furnace.util.Strings;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.reporting.config.HasClassification;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

/**
 * Adds the provided {@link HasClassification} operation to the current iteration filter.
 * <p>
 * Expected format:
 *
 * <pre>
 * &lt;has-classification title="parameterized classification {pattern}" /&gt;
 * </pre>
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@NamespaceElementHandler(elementName = "has-classification", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class HasClassificationHandler implements ElementHandler<HasClassification> {
    @Override
    public HasClassification processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String pattern = $(element).attr("title");
        HasClassification classification = new HasClassification();

        if (!Strings.isNullOrEmpty(pattern))
            classification.setTitlePattern(pattern);

        return classification;
    }
}
