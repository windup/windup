package org.jboss.windup.config.parser.xml;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.builder.RuleProviderBuilder;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.phase.RulePhase;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.joox.JOOX.$;

/**
 * Parses a "ruleset" element, and uses it to create a new {@link AbstractRuleProvider}
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = "ruleset", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class RuleProviderHandler implements ElementHandler<Void> {
    public static final String WINDUP_RULE_NAMESPACE = "http://windup.jboss.org/schema/jboss-ruleset";
    private static final String ID = "id";
    private static final String PHASE = "phase";
    private static final AtomicInteger currentDefaultIDIndex = new AtomicInteger(0);
    private Map<String, Class<? extends RulePhase>> cachedPhases;

    @Override
    public Void processElement(ParserContext context, Element element) throws ConfigurationException {
        String id = element.getAttribute(ID);
        if (StringUtils.isBlank(id)) {
            id = generateDefaultID();
        }
        RuleProviderBuilder builder = RuleProviderBuilder.begin(id);
        String phaseStr = element.getAttribute(PHASE);
        Class<? extends RulePhase> phase = getPhases().get(classNameToKey(phaseStr));
        builder.setPhase(phase);

        context.setBuilder(builder);

        List<Element> children = $(element).children().get();
        for (Element child : children) {
            context.processElement(child);
        }
        context.addRuleProvider(builder);

        return null;
    }

    private String generateDefaultID() {
        return "XMLRuleProvider:" + RandomStringUtils.random(4) + ":" + currentDefaultIDIndex.incrementAndGet();
    }

    private Map<String, Class<? extends RulePhase>> getPhases() {
        if (cachedPhases == null) {
            cachedPhases = new HashMap<>();
            Furnace furnace = FurnaceHolder.getFurnace();
            for (RulePhase phase : furnace.getAddonRegistry().getServices(RulePhase.class)) {
                @SuppressWarnings("unchecked")
                Class<? extends RulePhase> unwrappedClass = (Class<? extends RulePhase>) Proxies.unwrap(phase).getClass();
                String simpleName = unwrappedClass.getSimpleName();
                cachedPhases.put(classNameToKey(simpleName), unwrappedClass);
            }
        }
        return Collections.unmodifiableMap(cachedPhases);
    }

    private String classNameToKey(String className) {
        return className.toUpperCase();
    }
}
