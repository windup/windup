package org.jboss.windup.rules.apps.javaee.model.association;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;

@TypeValue(VendorSpecificationExtensionModel.TYPE)
public interface VendorSpecificationExtensionModel extends FileModel, SourceFileModel {
    String TYPE = "VendorSpecificationExtensionModel";
    String REF = "vendorSpecification";

    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = REF, direction = Direction.IN)
    FileModel getSpecificationFile();

    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = REF, direction = Direction.IN)
    void setSpecificationFile(FileModel fileReference);
}
