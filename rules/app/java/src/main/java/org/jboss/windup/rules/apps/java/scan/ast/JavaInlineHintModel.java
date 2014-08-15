package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.windup.reporting.model.InlineHintModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(JavaInlineHintModel.TYPE)
public interface JavaInlineHintModel extends InlineHintModel
{
    public static final String TYPE = "JavaInlineHintModel";
    public static final String JAVA_INLINE_HINT_TO_TYPE_REFERENCE = "JavaInlineHintToTypeReference";

    @Adjacency(label = JAVA_INLINE_HINT_TO_TYPE_REFERENCE, direction = Direction.OUT)
    public void setTypeReferenceModel(TypeReferenceModel m);

    @Adjacency(label = JAVA_INLINE_HINT_TO_TYPE_REFERENCE, direction = Direction.OUT)
    public TypeReferenceModel getTypeReferenceModel();
}
