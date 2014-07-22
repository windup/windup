package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ClassificationModel")
public interface ClassificationModel extends WindupVertexFrame
{
    public static final String PROPERTY_RULE_ID = "ruleID";
    public static final String PROPERTY_EFFORT = "effort";
    public static final String PROPERTY_LINK_DECORATOR = "linkDecorator";

    public static final String FILE_MODEL = "classificationModelToFileModel";

    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    public void setFileModel(FileModel fileModel);

    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    public FileModel getFileModel();

    /**
     * Sets the link decorators
     * 
     * @param linkDecorator
     */
    @Adjacency(label = PROPERTY_LINK_DECORATOR, direction = Direction.OUT)
    public void addLinkDecorator(LinkDecoratorModel linkDecorator);

    @Adjacency(label = PROPERTY_LINK_DECORATOR, direction = Direction.OUT)
    public Iterable<LinkDecoratorModel> getLinkDecorators();

    /**
     * Sets the effort needed to fix the issue
     * 
     * @param effort
     */
    @Property(PROPERTY_EFFORT)
    public void setEffort(int effort);

    @Property(PROPERTY_EFFORT)
    public int getEffort();

    /**
     * Sets the ID of the rule that triggered this particular blacklist entry
     * 
     * @param ruleID
     */
    @Property(PROPERTY_RULE_ID)
    public void setRuleID(String ruleID);

    @Property(PROPERTY_RULE_ID)
    public String getRuleID();

}
