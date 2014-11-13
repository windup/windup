package org.jboss.windup.config.parser.xml.when.gremlin;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.gremlinquery.GremlinQuery;
import org.jboss.windup.config.gremlinquery.GremlinStep;
import org.jboss.windup.config.gremlinquery.GremlinTypeFilter;
import org.jboss.windup.config.gremlinquery.HasExpectedType;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.w3c.dom.Element;

/**
 * Parses "query" elements to create a {@link GremlinQuery}.
 * 
 * @author jsightler
 */
@NamespaceElementHandler(elementName = "query", namespace = "http://windup.jboss.org/v1/xml")
public class GremlinQueryHandler implements ElementHandler<GremlinQuery>
{
    private static final String FROM = "from";
    private static final String TO = "to";

    @Override
    public GremlinQuery processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String inputVar = $(element).attr(FROM);
        String outputVar = $(element).attr(TO);

        GremlinQuery query = StringUtils.isBlank(inputVar) ? new GremlinQuery() : new GremlinQuery(inputVar);
        if (!StringUtils.isEmpty(outputVar))
            query.as(outputVar);

        boolean needsTypeFilter = StringUtils.isEmpty(inputVar);
        boolean typeFilterAdded = false;
        for (Element child : $(element).children().get())
        {
            GremlinStep step = handlerManager.processElement(child);
            if (step instanceof GremlinTypeFilter)
            {
                typeFilterAdded = true;
            }
            if (needsTypeFilter && !typeFilterAdded && step instanceof HasExpectedType)
            {
                typeFilterAdded = true;
                Class<? extends WindupVertexFrame> expectedType = ((HasExpectedType) step).getExpectedTypeHint();
                query.step(new GremlinTypeFilter(expectedType));
            }
            query.step(step);
        }

        return query;
    }
}
