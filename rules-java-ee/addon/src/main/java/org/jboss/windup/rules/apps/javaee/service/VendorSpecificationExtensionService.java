package org.jboss.windup.rules.apps.javaee.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.attribute.Text;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.frames.FramedVertexIterable;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.association.VendorSpecificationExtensionModel;

import java.util.logging.Logger;

/**
 * Contains methods for querying, updating, and deleting {@link VendorSpecificationExtensionModel}
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class VendorSpecificationExtensionService extends GraphService<VendorSpecificationExtensionModel> {
    private static final Logger LOG = Logger.getLogger(VendorSpecificationExtensionService.class.getName());
    final protected FileService fileService;

    public VendorSpecificationExtensionService(GraphContext context) {
        super(context, VendorSpecificationExtensionModel.class);

        fileService = new FileService(this.getGraphContext());
    }

    public Iterable<VendorSpecificationExtensionModel> getVendorSpecificationExtensions(FileModel model) {
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V(model.getElement());
        pipeline.out(VendorSpecificationExtensionModel.REF);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.textContains(VendorSpecificationExtensionModel.TYPE));
        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline.toList(), VendorSpecificationExtensionModel.class);
    }

    /**
     * Makes the file model a vendor extension, and references a local file (if exists)
     *
     * @param model
     * @param localFileName
     */
    public VendorSpecificationExtensionModel associateAsVendorExtension(FileModel model, String localFileName) {

        String pathToDescriptor = model.getFilePath();
        pathToDescriptor = StringUtils.removeEnd(pathToDescriptor, model.getFileName());
        pathToDescriptor += localFileName;

        // now look up the
        FileModel specificationFile = fileService.getUniqueByProperty(FileModel.FILE_PATH, pathToDescriptor);
        VendorSpecificationExtensionModel extension = addTypeToModel(model);

        if (specificationFile == null) {
            LOG.warning("File not found: " + pathToDescriptor);
        } else {
            // now associate current model with vendorspecificationextension
            extension.setSpecificationFile(specificationFile);
        }

        return extension;
    }
}
