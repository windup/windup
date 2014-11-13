package org.jboss.windup.config.parser.xml.when;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.IterationBuilderOver;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.ocpsoft.rewrite.config.Operation;
import org.w3c.dom.Element;

/**
 * Parses the "iteration" element to produce {@link Iteration} {@link Operation}s.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@NamespaceElementHandler(elementName = "iteration", namespace = "http://windup.jboss.org/v1/xml")
public class IterationHandler implements ElementHandler<Iteration>
{

    @Override
    public Iteration processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String overVar = $(element).attr("over");
        IterationBuilderOver iterationOver = StringUtils.isEmpty(overVar) ? Iteration.over() : Iteration.over(overVar);

        List<Element> children = $(element).children().get();
        List<Operation> operations = new ArrayList<>(children.size());
        for (Element child : children)
        {
            Operation operation = handlerManager.processElement(child);
            operations.add(operation);
        }
        iterationOver.perform(operations.toArray(new Operation[operations.size()]));
        return (Iteration) iterationOver;
    }
}
