package org.jboss.windup.rules.apps.javaee.model.association;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(VendorSpecificationExtensionModel.TYPE)
public interface VendorSpecificationExtensionModel extends FileModel
{
    public static final String TYPE = "VendorSpecificationReferenceModel";
    public static final String REF = "vendorSpecification";

    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = REF, direction = Direction.IN)
    public FileModel getSpecificationFile();

    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = REF, direction = Direction.IN)
    public void setSpecificationFile(FileModel fileReference);
}
