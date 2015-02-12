package org.jboss.windup.config.parser.xml.perform;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.IterationBuilderOtherwise;
import org.jboss.windup.config.operation.iteration.IterationBuilderOver;
import org.jboss.windup.config.operation.iteration.IterationBuilderPerform;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.Perform;
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
        List<Operation> otherwise = new ArrayList<>();
        for (Element child : children)
        {
            if ("when".equals(child.getNodeName()))
            {
                List<Element> whenElements = $(child).children().get();
                for (Element whenElement : whenElements)
                {
                    Object object = handlerManager.processElement(whenElement);
                    if (object instanceof Condition)
                    {
                        iterationOver.when((Condition) object);
                    }
                }
            }
            else if ("perform".equals(child.getNodeName()))
            {
                List<Element> performElements = $(child).children().get();
                for (Element performElement : performElements)
                {
                    Object object = handlerManager.processElement(performElement);
                    if (object instanceof Operation)
                    {
                        operations.add((Operation) object);
                    }
                }
            }
            else if ("otherwise".equals(child.getNodeName()))
            {
                List<Element> otherwiseElements = $(child).children().get();
                for (Element otherwiseElement : otherwiseElements)
                {
                    Object object = handlerManager.processElement(otherwiseElement);
                    if (object instanceof Operation)
                    {
                        otherwise.add((Operation) object);
                    }
                }
            }
            else
            {
                Object object = handlerManager.processElement(child);
                if (object instanceof Operation)
                {
                    operations.add((Operation) object);
                }
                else if (object instanceof Condition)
                {
                    iterationOver.when((Condition) object);
                }
            }
        }
        IterationBuilderPerform iterationBuilderPerform = iterationOver.perform(operations
                    .toArray(new Operation[operations.size()]));
        if (otherwise.size() > 0)
        {
            IterationBuilderOtherwise iterationBuilderOtherwise = iterationBuilderPerform.otherwise(Perform
                        .all(otherwise.toArray(new Operation[otherwise.size()])));
            return (Iteration) iterationBuilderOtherwise;
        }

        return (Iteration) iterationBuilderPerform;
    }
}