package org.jboss.windup.rules.apps.javaee;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.joox.JOOX.$;

/**
 * Creates {@link TechnologyUsageStatisticsModelExists} objects based upon the following format:
 *
 * <pre>
 *     &lt;technology-statistics-exists name="Technology" number-found="2" &gt;
 *         &lt;tag name=”View” /&gt;
 *         &lt;tag name=”Web” /&gt;
 *     &lt;/technology-statistics-exists&gt;
 *
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = TechnologyUsageStatisticsModelExistsHandler.ELEMENT_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class TechnologyUsageStatisticsModelExistsHandler implements ElementHandler<TechnologyUsageStatisticsModelExists> {
    public static final String ELEMENT_NAME = "technology-statistics-exists";

    @Override
    public TechnologyUsageStatisticsModelExists processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String technologyName = $(element).attr(TechnologyIdentifiedHandler.NAME);
        if (StringUtils.isBlank(technologyName)) {
            throw new WindupException("Error, '" + ELEMENT_NAME + "' element must have a non-empty '" + TechnologyIdentifiedHandler.NAME + "' attribute or element");
        }
        int count = TechnologyIdentified.DEFAULT_COUNT;
        String countStr = $(element).attr(TechnologyIdentifiedHandler.NUMBER_FOUND);

        if (StringUtils.isNotBlank(countStr)) {
            countStr = countStr.trim();
            try {
                count = Integer.parseInt(countStr);
            } catch (Exception e) {
                throw new WindupException(e);
            }
        }

        Set<String> tags = new HashSet<>();

        List<Element> children = $(element).children().get();
        for (Element child : children) {
            if (child.getNodeName().equals(TechnologyIdentifiedHandler.TAG)) {
                String tag = $(child).attr(TechnologyIdentifiedHandler.NAME);
                if (StringUtils.isBlank(tag))
                    continue;

                tags.add(tag);
            }
        }


        return new TechnologyUsageStatisticsModelExists(technologyName, count, tags);
    }
}
