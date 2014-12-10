package org.jboss.windup.rules.apps.xml.service;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.xml.model.MatchedXPathModel;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class MatchedXPathService extends GraphService<MatchedXPathModel>
{

    public MatchedXPathService(GraphContext context)
    {
        super(context, MatchedXPathModel.class);
    }

    public MatchedXPathModel create(String xpath, String fileName, String publicID, String xpathResultMatch)
    {
        MatchedXPathModel matchedXPathToModel = create();
        matchedXPathToModel.setXPath(xpath);
        if (!StringUtils.isBlank(fileName))
        {
            matchedXPathToModel.setFileName(fileName);
        }
        if (!StringUtils.isBlank(publicID))
        {
            matchedXPathToModel.setPublicID(publicID);
        }
        if (!StringUtils.isBlank(xpathResultMatch))
        {
            matchedXPathToModel.setXPathResultMatch(xpathResultMatch);
        }
        return matchedXPathToModel;
    }

    public MatchedXPathModel find(String xpath, String fileName, String publicID, String xpathResultMatch)
    {
        GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<>(getGraphContext().getGraph());
        pipe.V();
        pipe.has(WindupVertexFrame.TYPE_PROP, MatchedXPathModel.TYPE);
        pipe.has(MatchedXPathModel.XPATH, xpath);
        if (StringUtils.isEmpty(fileName))
        {
            pipe.hasNot(MatchedXPathModel.FILENAME);
        }
        else
        {
            pipe.has(MatchedXPathModel.FILENAME, fileName);
        }

        if (StringUtils.isEmpty(publicID))
        {
            pipe.hasNot(MatchedXPathModel.PUBLIC_ID);
        }
        else
        {
            pipe.has(MatchedXPathModel.PUBLIC_ID, publicID);
        }

        if (StringUtils.isEmpty(xpathResultMatch))
        {
            pipe.hasNot(MatchedXPathModel.XPATH_RESULT_MATCH);
        }
        else
        {
            pipe.has(MatchedXPathModel.XPATH_RESULT_MATCH, xpathResultMatch);
        }
        MatchedXPathModel result = null;
        for (Vertex v : pipe)
        {
            if (result != null)
            {
                // there should only be a single result, fail if it isn't a singleton
                throw new WindupException("Multiple " + MatchedXPathModel.class.getSimpleName() + "s found with params: xpath=" + xpath
                            + ", fileName: " + fileName + ", publicID: " + publicID + ", matchedXPathResult: " + xpathResultMatch);
            }
            result = frame(v);
        }
        return result;
    }
}
