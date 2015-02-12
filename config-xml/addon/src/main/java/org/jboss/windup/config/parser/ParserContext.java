package org.jboss.windup.config.parser;

import static org.joox.JOOX.$;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.builder.WindupRuleProviderBuilder;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.util.Annotations;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderPerform;
import org.ocpsoft.rewrite.config.ConfigurationRuleParameterWhere;
import org.w3c.dom.Element;

/**
 * Handles maintaining the list of handlers associated with each tag/namespace pair, as well as selecting the right
 * handler for element. This also maintains the current {@link WindupRuleProviderBuilder} being constructed.
 */
public class ParserContext
{
    private List<WindupRuleProvider> ruleProviders = new ArrayList<>();
    private WindupRuleProviderBuilder builder;
    private ConfigurationRuleBuilderPerform rule;
    private ConfigurationRuleParameterWhere where;
    private final Map<HandlerId, ElementHandler<?>> handlers = new HashMap<>();

    /**
     * The addon containing the xml file currently being parsed. This is needed mainly because of the classloader that loaded the Addon (XSLTTransformation needs it.)
     */
    private Addon addonContainingInputXML;
    /**
     * The folder containing the xml file currently being parse. This should be the root folder from which any other
     * resource lookups should be based. Eg, it may be the user scripts folder.
     * 
     * If this is set, it should take precedent over the Addon for resource lookups.
     */
    private Path xmlInputPath;

    /**
     * Initialize tag handlers based upon the provided furnace instance.
     */
    public ParserContext(Furnace furnace)
    {
        @SuppressWarnings("rawtypes")
        Imported<ElementHandler> loadedHandlers = furnace.getAddonRegistry().getServices(ElementHandler.class);
        for (ElementHandler<?> handler : loadedHandlers)
        {
            NamespaceElementHandler annotation = Annotations.getAnnotation(handler.getClass(),
                        NamespaceElementHandler.class);
            if (annotation != null)
            {
                HandlerId handlerID = new HandlerId(annotation.namespace(), annotation.elementName());
                if (handlers.containsKey(handlerID))
                {
                    String className1 = Proxies.unwrapProxyClassName(handlers.get(handlerID).getClass());
                    String className2 = Proxies.unwrapProxyClassName(handler.getClass());
                    throw new WindupException("Multiple handlers registered with id: " + handlerID + " Classes are: "
                                + className1 + " and " + className2);
                }
                handlers.put(handlerID, handler);
            }
        }
    }

    /**
     * Process the provided {@link Element} with the appropriate handler for it's namespace and tag name.
     */
    @SuppressWarnings("unchecked")
    public <T> T processElement(Element element) throws ConfigurationException
    {
        String namespace = $(element).namespaceURI();
        String tagName = $(element).tag();
        ElementHandler<?> handler = handlers.get(new HandlerId(namespace, tagName));
        if (handler != null)
        {
            Object o = handler.processElement(this, element);
            return (T) o;
        }
        throw new ConfigurationException("No Handler registered for element named [" + tagName
                    + "] in namespace: [" + namespace + "]");
    }

    /**
     * Gets a {@link List} of all {@link RuleProviders} found so far.
     */
    public List<WindupRuleProvider> getRuleProviders()
    {
        return this.ruleProviders;
    }

    /**
     * Adds the constructed {@link WindupRuleProvider}.
     */
    public void addRuleProvider(WindupRuleProvider provider)
    {
        this.ruleProviders.add(provider);
    }

    /**
     * Gets the {@link WindupRuleProviderBuilder} that is currently in the process of being built.
     */
    public WindupRuleProviderBuilder getBuilder()
    {
        return builder;
    }

    /**
     * Sets the {@link WindupRuleProviderBuilder} that is currently in the process of being built.
     */
    public void setBuilder(WindupRuleProviderBuilder builder)
    {
        this.builder = builder;
    }

    /**
     * Gets the current where conditional
     */
    public ConfigurationRuleParameterWhere getWhere()
    {
        return where;
    }

    /**
     * Sets the current where conditional
     */
    public void setWhere(ConfigurationRuleParameterWhere where)
    {
        this.where = where;
    }

    /**
     * Gets the current Rule
     */
    public void setRule(ConfigurationRuleBuilderPerform rule)
    {
        this.rule = rule;
    }

    /**
     * Sets the current Rule
     */
    public ConfigurationRuleBuilderPerform getRule()
    {
        return rule;
    }

    /**
     * The addon containing the xml file currently being parsed.
     */
    public void setAddonContainingInputXML(Addon addonContainingInputXML)
    {
        this.addonContainingInputXML = addonContainingInputXML;
    }

    /**
     * The addon containing the xml file currently being parsed.
     */
    public Addon getAddonContainingInputXML()
    {
        return addonContainingInputXML;
    }

    /**
     * The folder containing the xml file currently being parse. This should be the root folder from which any other
     * resource lookups should be based. Eg, it may be the user scripts folder.
     * 
     * If this is set, it should take precedent over the Addon for resource lookups.
     */
    public void setXmlInputPath(Path xmlInputPath)
    {
        this.xmlInputPath = xmlInputPath;
    }

    /**
     * The folder containing the xml file currently being parse. This should be the root folder from which any other
     * resource lookups should be based. Eg, it may be the user scripts folder.
     * 
     * If this is set, it should take precedent over the Addon for resource lookups.
     */
    public Path getXmlInputPath()
    {
        return xmlInputPath;
    }
}
