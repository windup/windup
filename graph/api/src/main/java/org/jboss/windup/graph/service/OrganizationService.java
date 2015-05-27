package org.jboss.windup.graph.service;

import org.apache.tools.ant.taskdefs.Length;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.OrganizationModel;
import org.jboss.windup.graph.model.resource.FileModel;

/**
 * Adds methods for organization model.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class OrganizationService extends GraphService<OrganizationModel>
{
    public OrganizationService(GraphContext context)
    {
        super(context, OrganizationModel.class);
    }

    /**
     * Attach a {@link OrganizationModel} with the given organization to the provided {@link ArchiveModel}. If an existing Model
     * exists with the provided organization, that one will be used instead.
     */
    public OrganizationModel attachOrganization(ArchiveModel archiveModel, String organizationName)
    {
        OrganizationModel model = getUnique(getTypedQuery().has(OrganizationModel.NAME, organizationName));
        if (model == null)
        {
            model = create();
            model.setName(organizationName);
            model.addArchiveModel(archiveModel);
        }
        else
        {
            return attachOrganization(model, archiveModel);
        }

        return model;
    }

    /**
     * This method just attaches the {@link OrganizationModel} to the {@link Length.FileMode}. It will only do so if this link is not already
     * present.
     */
    public OrganizationModel attachOrganization(OrganizationModel organizationModel, ArchiveModel archiveModel)
    {
        // check for duplicates
        for (FileModel existingFileModel : organizationModel.getArchiveModels())
        {
            if (existingFileModel.equals(archiveModel))
            {
                return organizationModel;
            }
        }
        organizationModel.addArchiveModel(archiveModel);
        return organizationModel;
    }
    

    /**
     * This method just attaches the {@link LinkModel} to the {@link ClassificationModel}. It will only do so if this link is not already
     * present.
     */
    public OrganizationModel attachLink(OrganizationModel organizationModel, LinkModel linkModel)
    {
        // check for duplicates
        for (LinkModel existing : organizationModel.getLinks())
        {
            if (existing.equals(linkModel))
            {
                return organizationModel;
            }
        }
        organizationModel.addLink(linkModel);
        return organizationModel;
    }
}
