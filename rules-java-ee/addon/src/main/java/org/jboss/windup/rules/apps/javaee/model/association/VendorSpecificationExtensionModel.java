package org.jboss.windup.rules.apps.javaee.model.association;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(VendorSpecificationExtensionModel.TYPE)
public interface VendorSpecificationExtensionModel extends FileModel, SourceFileModel
{
    String TYPE = "VendorSpecificationReference";
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
