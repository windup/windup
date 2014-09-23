package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.builder.WindupRuleProviderBuilder;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.w3c.dom.Element;

/**
 * Parses a "ruleset" element, and uses it to create a new {@link WindupRuleProvider}
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@NamespaceElementHandler(elementName = "ruleset", namespace = "http://windup.jboss.org/v1/xml")
public class RuleProviderHandler implements ElementHandler<Void>
{
    private static final String ID = "id";
    private static final AtomicInteger currentDefaultIDIndex = new AtomicInteger(0);

    @Override
    public Void processElement(ParserContext context, Element element) throws ConfigurationException
    {
        String id = element.getAttribute(ID);
        if (StringUtils.isBlank(id))
        {
            id = generateDefaultID();
        }
        WindupRuleProviderBuilder builder = WindupRuleProviderBuilder.begin(id);
        context.setBuilder(builder);

        List<Element> children = $(element).children().get();
        for (Element child : children)
        {
            Object result = context.processElement(child);

            switch ($(child).tag())
            {
            case "execute-after":
                builder.getExecuteAfterIDs().add(result.toString());
            case "execute-before":
                builder.getExecuteBeforeIDs().add(result.toString());
            }
        }
        context.addRuleProvider(builder);

        return null;
    }

    private String generateDefaultID()
    {
        return "XMLRuleProvider:" + RandomStringUtils.random(4) + ":" + currentDefaultIDIndex.incrementAndGet();
    }
}
