package org.jboss.windup.config.parser.xml.when;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.util.List;

import static org.joox.JOOX.$;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = QueryHandler.ELEMENT_NAME, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class QueryHandler implements ElementHandler<Query> {
    public static final String ELEMENT_NAME = "graph-query";
    public static final String DISCRIMINATOR = "discriminator";
    public static final String FROM = "from";
    public static final String AS = "as";
    public static final String PROPERTY = "property";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_SEARCH_TYPE = "searchType";

    @Inject
    private GraphTypeManager graphTypeManager;

    @Override
    public Query processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String from = $(element).attr(FROM);
        String as = $(element).attr(AS);
        String discriminator = $(element).attr(DISCRIMINATOR);
        if (StringUtils.isBlank(discriminator)) {
            throw new WindupException("Error loading rule, '" + DISCRIMINATOR + "' attribute must be specified!");
        }

        Query query = null;
        if (StringUtils.isNotBlank(from)) {
            query = (Query) Query.from(from);
        }

        Class<? extends WindupFrame> typeAsWindupFrame = this.graphTypeManager.getTypeForDiscriminator(discriminator);
        if (typeAsWindupFrame == null)
            throw new WindupException("Error, type: " + discriminator + " not registered!");

        Class<? extends WindupVertexFrame> type = cast(typeAsWindupFrame);
        if (query == null) {
            query = (Query) Query.fromType(type);
        } else {
            query = (Query) query.includingType(type);
        }

        List<Element> children = $(element).children().get();
        for (Element child : children) {
            if (child.getNodeName().equals(PROPERTY)) {
                String propertyName = $(child).attr(PROPERTY_NAME);
                if (StringUtils.isBlank(propertyName))
                    continue;

                String value = $(child).text();
                String propertyType = $(child).attr(PROPERTY_TYPE);
                String searchMode = $(child).attr(PROPERTY_SEARCH_TYPE);

                if (StringUtils.equals("BOOLEAN", propertyType)) {
                    query.withProperty(propertyName, Boolean.valueOf(value));
                } else {
                    if (StringUtils.equals("regex", searchMode))
                        query.withProperty(propertyName, QueryPropertyComparisonType.REGEX, value);
                    else
                        query.withProperty(propertyName, value);
                }
            }
        }

        if (StringUtils.isNotBlank(as))
            query.as(as);

        return query;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends WindupVertexFrame> cast(Class<? extends WindupFrame> typeAsWindupFrame) {
        if (!WindupVertexFrame.class.isAssignableFrom(typeAsWindupFrame))
            throw new WindupException("Only types that are a subclass of " + WindupVertexFrame.class.getCanonicalName() + " are supported by the "
                    + ELEMENT_NAME + " element!");

        return (Class<? extends WindupVertexFrame>) typeAsWindupFrame;
    }
}
