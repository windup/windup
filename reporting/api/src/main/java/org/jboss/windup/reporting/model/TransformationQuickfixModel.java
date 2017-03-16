package org.jboss.windup.reporting.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This contains information for a quickfix that will use a custom transformer class.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(TransformationQuickfixModel.TYPE_VALUE)
public interface TransformationQuickfixModel extends QuickfixModel {
    String TYPE_VALUE = "TransformationQuickfixModel";

    String TRANSFORMATION_ID = TYPE_VALUE + "-transformationID";

    /**
     * Contains the ID of the class that will implement this translation.
     */
    @Property(TRANSFORMATION_ID)
    void setTransformationID(String transformationID);

    /**
     * Contains the ID of the class that will implement this translation.
     */
    @Property(TRANSFORMATION_ID)
    String getTransformationID();
}
