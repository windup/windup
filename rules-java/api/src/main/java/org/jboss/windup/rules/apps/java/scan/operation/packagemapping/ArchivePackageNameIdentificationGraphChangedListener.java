package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import java.util.List;
import java.util.logging.Logger;

import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.MutationListener;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphListener;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.graph.model.IgnoredArchiveModel;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.util.Logging;

import static org.jboss.windup.rules.apps.java.scan.operation.packagemapping.PackageNameMapping.*;

/**
 * {@link MutationListener} responsible for identifying {@link ArchiveModel} instances based upon their contained package names.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jess Sightler</a>
 */
public class ArchivePackageNameIdentificationGraphChangedListener implements GraphListener
{
    private static Logger LOG = Logging.get(ArchivePackageNameIdentificationGraphChangedListener.class);

    private GraphRewrite event;

    public ArchivePackageNameIdentificationGraphChangedListener(GraphRewrite event)
    {
        this.event = event;
    }

    @Override
    public void vertexPropertyChanged(Vertex vertex, Property property, Object oldValue, Object... setValue)
    {
        try
        {
            if (ArchiveModel.ARCHIVE_NAME.equals(property.key()))
            {
                ArchiveService archiveService = new ArchiveService(event.getGraphContext());
                ArchiveModel archive = archiveService.getById(vertex.id());

                // archive has already been identified, just ignore it
                if (archive instanceof IgnoredArchiveModel || archive instanceof IdentifiedArchiveModel)
                    return;

                WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                // If the forceKnownLibrariesAnalysis option is enabled, do not ignore it
                if (cfg.isAnalyzeKnownLibraries())
                    return;

                if (allPackagesAreKnown(archive))
                {
                    IgnoredFileModel ignoredFileModel = new GraphService<>(event.getGraphContext(), IgnoredFileModel.class).addTypeToModel(archive);
                    ignoredFileModel.setIgnoredRegex("3rd Party Archive");
                    new GraphService<>(event.getGraphContext(), IdentifiedArchiveModel.class).addTypeToModel(archive);
                }
            }
        }
        catch (Throwable t)
        {
            LOG.warning("Failed to check package name mapping due to: " + t.getMessage());
        }
    }

    private boolean allPackagesAreKnown(ArchiveModel archive) {
        // check if it can be ignored as a vendor archive
        boolean allPackagesAreKnown = areAllPackagesKnown(event, archive.getFilePath());

        // If this is a file that the user specified as the input application, do not ignore it
        List<FileModel> inputPaths = WindupConfigurationService.getConfigurationModel(this.event.getGraphContext()).getInputPaths();
        allPackagesAreKnown = allPackagesAreKnown && inputPaths.stream().noneMatch(inputPath -> inputPath.equals(archive));

        return allPackagesAreKnown;
    }

    @Override
    public void vertexAdded(Vertex vertex)
    {
    }

}
