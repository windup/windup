package org.jboss.windup.rules.apps.javaee;

import static org.joox.JOOX.$;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Indicates that the specified technology has been identified in the application.
 * <p>
 * Expected format:
 *
 * <pre>
 *   &lt;technology-identified number-found=”1”&gt;
 *       &lt;tag name=”View” /&gt;
 *       &lt;tag name=”Web” /&gt;
 *   &lt;/technology-identified&gt;
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = TechnologyIdentifiedHandler.TAG_NAME, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class TechnologyIdentifiedHandler implements ElementHandler<TechnologyIdentified> {
    public static final String TAG_NAME = "technology-identified";
    public static final String NAME = "name";
    public static final String TAG = "tag";
    public static final String NUMBER_FOUND = "number-found";
    private static final Logger LOG = Logger.getLogger(TechnologyIdentified.class.getName());

    @Override
    public TechnologyIdentified processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String technologyName = $(element).attr(NAME);
        if (StringUtils.isBlank(technologyName)) {
            throw new WindupException("Error, '" + TAG_NAME + "' element must have a non-empty '" + NAME + "' attribute or element");
        }
        int count = TechnologyIdentified.DEFAULT_COUNT;
        String countStr = $(element).attr(NUMBER_FOUND);
        if (StringUtils.isNotBlank(countStr)) {
            countStr = countStr.trim();
            try {
                count = Integer.parseInt(countStr);
            } catch (Exception e) {
                LOG.warning("Could not parse count string '" + countStr + "' for technology identifier: " + technologyName);
            }
        }

        Set<String> tags = new HashSet<>();

        List<Element> children = $(element).children().get();
        for (Element child : children) {
            if (child.getNodeName().equals(TAG)) {
                String tag = $(child).attr(NAME);
                if (StringUtils.isBlank(tag))
                    continue;

                tags.add(tag);
            }
        }

        TechnologyIdentifiedWithCount withCount = TechnologyIdentified.named(technologyName).numberFound(count);
        tags.forEach(withCount::withTag);
        return (TechnologyIdentified) withCount;
    }
}
