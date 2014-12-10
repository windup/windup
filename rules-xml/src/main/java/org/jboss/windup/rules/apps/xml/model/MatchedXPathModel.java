package org.jboss.windup.rules.apps.xml.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(MatchedXPathModel.TYPE)
public interface MatchedXPathModel extends WindupVertexFrame
{
    public static final String XPATH_RESULT_MATCH = "xpathResultMatch";
    public static final String PUBLIC_ID = "publicID";
    public static final String MATCHED_XPATH_TO_MODEL = "matchedxpathtomodel";
    public static final String FILENAME = "fileName";
    public static final String XPATH = "xpath";
    public static final String TYPE = "XmlFileModelMatchedXPathModel";

    @Property(FILENAME)
    String getFileName();

    @Property(FILENAME)
    void setFileName(String fileName);

    @Property(PUBLIC_ID)
    String getPublicID();

    @Property(PUBLIC_ID)
    void setPublicID(String publicID);

    @Property(XPATH_RESULT_MATCH)
    String getXPathResultMatch();

    @Property(XPATH_RESULT_MATCH)
    void setXPathResultMatch(String match);

    @Property(XPATH)
    String getXPath();

    @Property(XPATH)
    void setXPath(String xpath);

    @Adjacency(label = MATCHED_XPATH_TO_MODEL, direction = Direction.OUT)
    Iterable<XmlFileModel> getMatchedModels();

    @Adjacency(label = MATCHED_XPATH_TO_MODEL, direction = Direction.OUT)
    void addMatchedModel(XmlFileModel model);
}
