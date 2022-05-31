package org.jboss.windup.config.parser.xml;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.phase.RulePhaseFinder;
import org.w3c.dom.Element;

import java.util.stream.Collectors;

/**
 * Sets the phase for the current ruleset.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = "phase", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class PhaseHandler implements ElementHandler<Void> {

    private RulePhaseFinder phaseFinder = new RulePhaseFinder();

    @Override
    public Void processElement(ParserContext context, Element element) throws ConfigurationException {
        String phaseStr = element.getTextContent().trim();

        Class<? extends RulePhase> phase = phaseFinder.findPhase(phaseStr);
        if (phase == null) {
            String phasesListing = phaseFinder.getAvailablePhases().stream().map(clazz -> clazz.getSimpleName()).collect(Collectors.joining(System.lineSeparator() + "    "));
            throw new IllegalArgumentException("Unrecognized phase \"" + phaseStr + "\". Available phases: \n    " + phasesListing);
        }
        context.getBuilder().setPhase(phase);
        return null;
    }

}
