package org.jboss.windup.graph.service;

import org.apache.tools.ant.taskdefs.Length;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.VendorModel;
import org.jboss.windup.graph.model.resource.FileModel;

/**
 * Adds methods for vendor model.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Brad Davis</a>
 * 
 */
public class VendorService extends GraphService<VendorModel>
{
    public VendorService(GraphContext context)
    {
        super(context, VendorModel.class);
    }

    /**
     * Attach a {@link VendorModel} with the given vendor to the provided {@link ArchiveModel}. If an existing Model
     * exists with the provided vendor, that one will be used instead.
     */
    public VendorModel attachVendor(ArchiveModel archiveModel, String vendorName)
    {
        VendorModel model = getUnique(getTypedQuery().has(VendorModel.NAME, vendorName));
        if (model == null)
        {
            model = create();
            model.setName(vendorName);
            model.addArchiveModel(archiveModel);
        }
        else
        {
            return attachVendor(model, archiveModel);
        }

        return model;
    }

    /**
     * This method just attaches the {@link VendorModel} to the {@link Length.FileMode}. It will only do so if this link is not already
     * present.
     */
    public VendorModel attachVendor(VendorModel vendorModel, ArchiveModel archiveModel)
    {
        // check for duplicates
        for (FileModel existingFileModel : vendorModel.getArchiveModels())
        {
            if (existingFileModel.equals(archiveModel))
            {
                return vendorModel;
            }
        }
        vendorModel.addArchiveModel(archiveModel);
        return vendorModel;
    }
    

    /**
     * This method just attaches the {@link LinkModel} to the {@link ClassificationModel}. It will only do so if this link is not already
     * present.
     */
    public VendorModel attachLink(VendorModel vendorModel, LinkModel linkModel)
    {
        // check for duplicates
        for (LinkModel existing : vendorModel.getLinks())
        {
            if (existing.equals(linkModel))
            {
                return vendorModel;
            }
        }
        vendorModel.addLink(linkModel);
        return vendorModel;
    }
}
