package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import java.util.Map;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.archives.model.IgnoredArchiveModel;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;
import org.jboss.windup.util.Logging;

/**
 * {@link GraphChangedListener} responsible for identifying {@link ArchiveModel} instances based upon their contained package names.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jess Sightler</a>
 */
public class ArchivePackageNameIdentificationGraphChangedListener implements GraphChangedListener
{
    private static Logger LOG = Logging.get(ArchivePackageNameIdentificationGraphChangedListener.class);

    private GraphRewrite event;

    public ArchivePackageNameIdentificationGraphChangedListener(GraphRewrite event)
    {
        this.event = event;
    }

    @Override
    public void vertexPropertyChanged(Vertex vertex, String key, Object oldValue, Object setValue)
    {
        try
        {
            if (ArchiveModel.ARCHIVE_NAME.equals(key))
            {
                ArchiveService archiveService = new ArchiveService(event.getGraphContext());
                ArchiveModel archive = archiveService.frame(vertex);

                // archive has already been identified, just ignore it
                if (archive instanceof IgnoredArchiveModel || archive instanceof IdentifiedArchiveModel)
                    return;

                // check if it can be ignored as a vendor archive
                boolean exclusivelyKnown = PackageNameMapping.isExclusivelyKnownArchive(event, archive.getFilePath());

                // If this is a file that the user specified as the input application, do not ignore it
                for (FileModel inputFile : WindupConfigurationService.getConfigurationModel(this.event.getGraphContext()).getInputPaths())
                {
                    if (inputFile.equals(archive))
                        exclusivelyKnown = false;
                }

                if (exclusivelyKnown)
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

    @Override
    public void vertexPropertyRemoved(Vertex vertex, String key, Object removedValue)
    {
    }

    @Override
    public void vertexAdded(Vertex vertex)
    {
    }

    @Override
    public void vertexRemoved(Vertex vertex, Map<String, Object> props)
    {
    }

    @Override
    public void edgeAdded(Edge edge)
    {
    }

    @Override
    public void edgePropertyChanged(Edge edge, String key, Object oldValue, Object setValue)
    {
    }

    @Override
    public void edgePropertyRemoved(Edge edge, String key, Object removedValue)
    {
    }

    @Override
    public void edgeRemoved(Edge edge, Map<String, Object> props)
    {
    }

}
