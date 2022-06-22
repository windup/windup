package org.jboss.windup.config.parser;

import static org.joox.JOOX.$;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.furnace.util.Annotations;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.builder.RuleProviderBuilder;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleParameterWhere;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles maintaining the list of handlers associated with each tag/namespace pair, as well as selecting the right
 * handler for element. This also maintains the current {@link RuleProviderBuilder} being constructed.
 */
public class ParserContext {
    private final List<AbstractRuleProvider> ruleProviders = new ArrayList<>();
    private RuleProviderBuilder builder;
    private ConfigurationRuleBuilder rule;
    private ConfigurationRuleParameterWhere where;
    private final Map<HandlerId, ElementHandler<?>> handlers = new HashMap<>();
    private final RuleLoaderContext ruleLoaderContext;

    /**
     * The addon containing the xml file currently being parsed. This is needed mainly because of the classloader that
     * loaded the Addon (XSLTTransformation needs it.)
     */
    private Addon addonContainingInputXML;
    /**
     * The folder containing the xml file currently being parse. This should be the root folder from which any other
     * resource lookups should be based. Eg, it may be the user scripts folder.
     * <p>
     * If this is set, it should take precedent over the Addon for resource lookups.
     */
    private Path xmlInputRootPath;

    private Path xmlInputPath;

    /**
     * Initialize tag handlers based upon the provided furnace instance.
     */
    public ParserContext(Furnace furnace, RuleLoaderContext ruleLoaderContext) {
        this.ruleLoaderContext = ruleLoaderContext;

        @SuppressWarnings("rawtypes")
        Imported<ElementHandler> loadedHandlers = furnace.getAddonRegistry().getServices(ElementHandler.class);
        for (ElementHandler<?> handler : loadedHandlers) {
            NamespaceElementHandler annotation = Annotations.getAnnotation(handler.getClass(),
                    NamespaceElementHandler.class);
            if (annotation != null) {
                HandlerId handlerID = new HandlerId(annotation.namespace(), annotation.elementName());
                if (handlers.containsKey(handlerID)) {
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
    public <T> T processElement(Element element) throws ConfigurationException {
        String namespace = $(element).namespaceURI();
        String tagName = $(element).tag();
        ElementHandler<?> handler = handlers.get(new HandlerId(namespace, tagName));
        if (handler != null) {
            Object o = handler.processElement(this, element);
            return (T) o;
        }
        throw new ConfigurationException("No Handler registered for element named [" + tagName
                + "] in namespace: [" + namespace + "]");
    }

    /**
     * Processes the XML document at the provided {@link URL} and returns a result from the namespace element handlers.
     */
    public <T> T processDocument(URI uri) throws ConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = null;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (Exception e) {
            throw new WindupException("Failed to build xml parser due to: " + e.getMessage(), e);
        }

        try {
            Document doc = dBuilder.parse(uri.toString());
            return processElement(doc.getDocumentElement());
        } catch (Exception e) {
            throw new WindupException("Failed to parse document at: " + uri + ", due to: " + e.getMessage(), e);
        }

    }

    /**
     * Gets a {@link List} of all {@link AbstractRuleProvider}s found so far.
     */
    public List<AbstractRuleProvider> getRuleProviders() {
        return this.ruleProviders;
    }

    /**
     * Adds the constructed {@link AbstractRuleProvider}.
     */
    public void addRuleProvider(AbstractRuleProvider provider) {
        this.ruleProviders.add(provider);
    }

    /**
     * Gets the {@link RuleProviderBuilder} that is currently in the process of being built.
     */
    public RuleProviderBuilder getBuilder() {
        return builder;
    }

    /**
     * Sets the {@link RuleProviderBuilder} that is currently in the process of being built.
     */
    public void setBuilder(RuleProviderBuilder builder) {
        this.builder = builder;
    }

    /**
     * Gets the current where conditional
     */
    public ConfigurationRuleParameterWhere getWhere() {
        return where;
    }

    /**
     * Sets the current where conditional
     */
    public void setWhere(ConfigurationRuleParameterWhere where) {
        this.where = where;
    }

    /**
     * Sets the current Rule
     */
    public void setRule(ConfigurationRuleBuilder rule) {
        this.rule = rule;
    }

    /**
     * Gets the current Rule
     */
    public ConfigurationRuleBuilder getRule() {
        return rule;
    }

    /**
     * The addon containing the xml file currently being parsed.
     */
    public void setAddonContainingInputXML(Addon addonContainingInputXML) {
        this.addonContainingInputXML = addonContainingInputXML;
    }

    /**
     * The addon containing the xml file currently being parsed.
     */
    public Addon getAddonContainingInputXML() {
        return addonContainingInputXML;
    }

    /**
     * The path to the rule xml file itself (eg, /path/to/rule.windup.xml or /path/to/rule.rhamt.xml).
     */
    public void setXmlInputPath(Path xmlInputPath) {
        this.xmlInputPath = xmlInputPath;
    }

    /**
     * The path to the rule xml file itself (eg, /path/to/rule.windup.xml or /path/to/rule.rhamt.xml).
     */
    public Path getXmlInputPath() {
        return this.xmlInputPath;
    }

    /**
     * The folder containing the xml file currently being parsed. This should be the root folder from which any other
     * resource lookups should be based. Eg, it may be the user scripts folder.
     * <p>
     * If this is set, it should take precedent over the Addon for resource lookups.
     */
    public void setXmlInputRootPath(Path xmlRootInputPath) {
        this.xmlInputRootPath = xmlRootInputPath;
    }

    /**
     * The folder containing the xml file currently being parsed. This should be the root folder from which any other
     * resource lookups should be based. Eg, it may be the user scripts folder.
     * <p>
     * If this is set, it should take precedent over the Addon for resource lookups.
     */
    public Path getXmlInputRootPath() {
        return xmlInputRootPath;
    }

    /**
     * Get access to rule loader information (paths, context variables, etc).
     */
    public RuleLoaderContext getRuleLoaderContext() {
        return ruleLoaderContext;
    }
}
