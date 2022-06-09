package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;


@TypeValue(LineMappingModel.TYPE)
public interface LineMappingModel extends WindupVertexFrame {
        String TYPE = "LineMappingModel";
        String ENCODED_MAPPING = "encodedMapping";

        /**
         * Indicates whether the class is declared "public".
         */
        @Property(ENCODED_MAPPING)
        String getEncodedMapping();

        /**
         * Indicates whether the class is declared "public".
         */
        @Property(ENCODED_MAPPING)
        void setEncodedMapping(String s);

}
