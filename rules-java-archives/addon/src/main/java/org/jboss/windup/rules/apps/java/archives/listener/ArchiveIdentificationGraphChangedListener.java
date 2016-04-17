package org.jboss.windup.rules.apps.java.archives.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tools.ant.util.PackageNameMapper;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.archives.identify.ArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.archives.model.IgnoredArchiveModel;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;
import org.jboss.windup.util.Logging;

/**
 * {@link GraphChangedListener} responsible for identifying {@link ArchiveModel} instances when they are added to the graph.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public final class ArchiveIdentificationGraphChangedListener implements GraphChangedListener
{
    private static final Logger LOG = Logging.get(ArchiveIdentificationGraphChangedListener.class);

    private final ArchiveIdentificationService identifier;
    private GraphContext context;
    private ArchiveService archiveService;

    public ArchiveIdentificationGraphChangedListener setGraphContext(GraphContext context)
    {
        this.context = context;
        this.archiveService = new ArchiveService(context);
        return this;
    }

    public ArchiveIdentificationGraphChangedListener(GraphContext context, ArchiveIdentificationService identifier)
    {
        this.identifier = identifier;
        this.setGraphContext(context);
    }

    @Override
    public void vertexPropertyChanged(Vertex vertex, String key, Object oldValue, Object setValue)
    {
        if (ArchiveModel.ARCHIVE_NAME.equals(key))
        {
            ArchiveModel archive = archiveService.frame(vertex);

            setArchiveHashes(archive);

            Coordinate coordinate = identifier.getCoordinate(archive.getSHA1Hash());
            if (coordinate != null)
            {
                LOG.info("Identified archive: [" + archive.getFilePath() + "] as [" + coordinate + "] will not be unzipped or analyzed.");
                IdentifiedArchiveModel identifiedArchive = GraphService.addTypeToModel(context, archive, IdentifiedArchiveModel.class);
                ArchiveCoordinateModel coordinateModel = new GraphService<>(context, ArchiveCoordinateModel.class).create();

                coordinateModel.setArtifactId(coordinate.getArtifactId());
                coordinateModel.setGroupId(coordinate.getGroupId());
                coordinateModel.setVersion(coordinate.getVersion());
                coordinateModel.setClassifier(coordinate.getClassifier());

                identifiedArchive.setCoordinate(coordinateModel);
                IgnoredArchiveModel ignoredArchive = GraphService.addTypeToModel(context, archive, IgnoredArchiveModel.class);
                ignoredArchive.setIgnoredRegex("Known open-source library");
            }
            else
            {
                LOG.info("Archive not identified: " + archive.getFilePath() + " SHA1: " + archive.getSHA1Hash());
            }
        }
    }

    private void setArchiveHashes(ArchiveModel payload)
    {
        if (payload.getMD5Hash() == null)
        {
            try (InputStream is = payload.asInputStream())
            {
                String md5 = DigestUtils.md5Hex(is);
                payload.setMD5Hash(md5);
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to read archive file at: " + payload.getFilePath() + " due to: " + e.getMessage(), e);
            }
        }

        if (payload.getSHA1Hash() == null)
        {
            try (InputStream is = payload.asInputStream())
            {
                String sha1 = DigestUtils.sha1Hex(is);
                payload.setSHA1Hash(sha1);
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to read archive file at: " + payload.getFilePath() + " due to: " + e.getMessage(), e);
            }
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
