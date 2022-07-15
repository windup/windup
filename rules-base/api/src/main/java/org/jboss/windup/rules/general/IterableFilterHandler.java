package org.jboss.windup.rules.general;

import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

import java.util.List;
import java.util.regex.Pattern;

import static org.joox.JOOX.$;

/**
 * An {@link ElementHandler} for {@link IterableFilter}
 * <p>
 * Example:
 *
 * <pre>
 * &lt;iterable-filter size="3" &gt;
 *   &lt;hint ...../&gt;
 * &lt;/iterable-filter &gt;
 * </pre>
 */
@NamespaceElementHandler(elementName = IterableFilterHandler.ELEMENT_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class IterableFilterHandler implements ElementHandler<IterableFilter> {
    public static final String ELEMENT_NAME = "iterable-filter";
    public static final String SIZE_ATTRIBUTE = "size";

    @Override
    public IterableFilter processElement(ParserContext context, Element element) throws ConfigurationException {
        // Read & Validate
        String sizeAttr = $(element).attr(SIZE_ATTRIBUTE);
        List<Element> children = $(element).children().get();
        validateAttributes(sizeAttr);
        validateChildren(children);
        Element firstChild = children.get(0);
        Object wrappedCondition = context.processElement(firstChild);
        validateWrappedCondition(wrappedCondition);

        // Create the condition
        IterableFilter iterableFilter = new IterableFilter(Integer.parseInt(sizeAttr));
        for (Element child : children) {
            Object condition = context.processElement(child);
            iterableFilter.withWrappedCondition((GraphCondition) condition);
        }

        return iterableFilter;
    }

    private void validateWrappedCondition(Object wrappedCondition) {
        if (!(wrappedCondition instanceof GraphCondition)) {
            throw new WindupException("The <" + ELEMENT_NAME + "> element must wrap GraphConditions only.");
        }
    }

    private void validateAttributes(String size) throws WindupException {
        if (!Pattern.matches("[0-9]+", size)) {
            throw new WindupException("The <" + ELEMENT_NAME + "> element's '" + IterableFilterHandler.SIZE_ATTRIBUTE + " attribute is not a valid number.");
        }
    }

    private void validateChildren(List<Element> children) throws WindupException {
        if (children.size() != 1) {
            throw new WindupException("The <" + ELEMENT_NAME + "> element must have exactly 1 child.");
        }
    }
}