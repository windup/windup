package org.jboss.windup.reporting.xml;

import org.jboss.forge.furnace.util.Strings;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.reporting.config.HasHint;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

/**
 * Adds the provided {@link HasHint} operation to the current iteration filter.
 * <p>
 * Expected format:
 *
 * <pre>
 * &lt;has-hint title="parameterized hint message {pattern}" /&gt;
 * </pre>
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@NamespaceElementHandler(elementName = "has-hint", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class HasHintHandler implements ElementHandler<HasHint> {
    @Override
    public HasHint processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String pattern = $(element).attr("message");
        HasHint hint = new HasHint();

        if (!Strings.isNullOrEmpty(pattern))
            hint.setMessagePattern(pattern);

        return hint;
    }
}
