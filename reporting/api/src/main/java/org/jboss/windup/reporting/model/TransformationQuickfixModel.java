package org.jboss.windup.reporting.model;

import static org.jboss.windup.reporting.model.TransformationQuickfixModel.TYPE_VALUE;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(TYPE_VALUE)
public interface TransformationQuickfixModel extends QuickfixModel
{
    String TYPE_VALUE = "TransformationQuickfix";
    String PROPERTY_TYPE = TYPE_VALUE + "-type";
    String CHANGES = TYPE_VALUE + "-changes";
    String HINT = TYPE_VALUE + "-hint";
    
    @Adjacency(label = HINT, direction = Direction.OUT)
    void setHintModel(InlineHintModel m);

    @Adjacency(label = HINT, direction = Direction.OUT)
    InlineHintModel getHintModel();

    @Adjacency(label = CHANGES, direction = Direction.OUT)
    void addChange(TransformationQuickfixChangeModel changeModel);

    @Adjacency(label = CHANGES, direction = Direction.OUT)
    Iterable<TransformationQuickfixChangeModel> getChanges();
}
