package org.jboss.windup.config.parser.xml;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.w3c.dom.Element;

/**
 * Sets the phase for the current ruleset
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@NamespaceElementHandler(elementName = "phase", namespace = "http://windup.jboss.org/v1/xml")
public class PhaseHandler implements ElementHandler<Void>
{

    @Override
    public Void processElement(ParserContext context, Element element) throws ConfigurationException
    {
        String phaseStr = element.getTextContent().trim();
        RulePhase phase = Enum.valueOf(RulePhase.class, phaseStr);
        context.getBuilder().setPhase(phase);
        return null;
    }
}
