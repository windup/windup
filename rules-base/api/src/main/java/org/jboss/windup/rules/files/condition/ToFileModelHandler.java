package org.jboss.windup.rules.files.condition;

import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.exception.WindupException;
import org.joox.JOOX;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Example:
 *
 * <pre>
 * &lt;toFileModel &gt;
 *   &lt;classification ...../&gt;
 * &lt;/toFileModel &gt;
 * </pre>
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@NamespaceElementHandler(elementName = ToFileModelHandler.ELEMENT_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class ToFileModelHandler implements ElementHandler<ToFileModel> {
    static final String ELEMENT_NAME = "to-file-model";

    @Override
    public ToFileModel processElement(ParserContext context, Element element) throws ConfigurationException {
        List<Element> children = JOOX.$(element).children().get();
        validateChildren(children);
        Element firstChild = children.get(0);
        GraphCondition wrappedCondition = (GraphCondition) context.processElement(firstChild);

        validateWrappedCondition(wrappedCondition);

        return ToFileModel.withWrappedCondition(wrappedCondition);
    }

    private void validateWrappedCondition(Object wrappedCondition) {
        if (!(wrappedCondition instanceof GraphCondition)) {
            throw new WindupException("The <" + ELEMENT_NAME + "> element must wrap GraphConditions only.");
        }
    }

    private void validateChildren(List<Element> children) {
        if (children.size() != 1) {
            throw new WindupException("The <" + ELEMENT_NAME + "> element must have exactly 1 child.");
        }
    }
}