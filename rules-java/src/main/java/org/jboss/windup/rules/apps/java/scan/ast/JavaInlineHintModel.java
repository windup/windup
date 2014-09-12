package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.InlineHintModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This is used to classify lines within application source {@link FileModel} instances, and to provide hints and
 * related data regarding specific positions within those files.
 * 
 * As this version is Java-specific, it also provides access to the TypeReferenceModel in the source code that is
 * associated with this Hint.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(JavaInlineHintModel.TYPE)
public interface JavaInlineHintModel extends InlineHintModel
{
    public static final String TYPE = "JavaInlineHintModel";
    public static final String JAVA_INLINE_HINT_TO_TYPE_REFERENCE = "JavaInlineHintToTypeReference";

    /**
     * Sets the TypeReferenceModel associated with this JavaInlineHintModel
     */
    @Adjacency(label = JAVA_INLINE_HINT_TO_TYPE_REFERENCE, direction = Direction.OUT)
    public void setTypeReferenceModel(TypeReferenceModel m);

    /**
     * Gets the TypeReferenceModel associated with this JavaInlineHintModel
     */
    @Adjacency(label = JAVA_INLINE_HINT_TO_TYPE_REFERENCE, direction = Direction.OUT)
    public TypeReferenceModel getTypeReferenceModel();
}
