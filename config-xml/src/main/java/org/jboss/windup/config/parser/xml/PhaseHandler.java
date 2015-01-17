package org.jboss.windup.config.parser.xml;

import java.util.HashMap;
import java.util.Map;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.phase.RulePhase;
import org.w3c.dom.Element;

/**
 * Sets the phase for the current ruleset
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@NamespaceElementHandler(elementName = "phase", namespace = "http://windup.jboss.org/v1/xml")
public class PhaseHandler implements ElementHandler<Void>
{

    private Map<String, Class<? extends RulePhase>> cachedPhases;

    @Override
    public Void processElement(ParserContext context, Element element) throws ConfigurationException
    {
        String phaseStr = element.getTextContent().trim();

        Class<? extends RulePhase> phase = getPhases().get(classNameToKey(phaseStr));
        if (phase == null)
        {
            throw new IllegalArgumentException("Unrecognized phase \"" + phase + "\"");
        }
        context.getBuilder().setPhase(phase);
        return null;
    }

    private String classNameToKey(String className)
    {
        return className.toUpperCase();
    }

    private Map<String, Class<? extends RulePhase>> getPhases()
    {
        if (cachedPhases == null)
        {
            cachedPhases = new HashMap<>();
            Furnace furnace = FurnaceHolder.getFurnace();
            for (RulePhase phase : furnace.getAddonRegistry().getServices(RulePhase.class))
            {
                @SuppressWarnings("unchecked")
                Class<? extends RulePhase> unwrappedClass = (Class<? extends RulePhase>) Proxies.unwrap(phase).getClass();
                String simpleName = unwrappedClass.getSimpleName();
                cachedPhases.put(classNameToKey(simpleName), unwrappedClass);
            }
        }
        return cachedPhases;
    }
}
