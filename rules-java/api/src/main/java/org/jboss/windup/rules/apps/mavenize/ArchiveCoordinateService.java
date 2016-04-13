package org.jboss.windup.rules.apps.mavenize;


import java.util.Iterator;
import java.util.logging.Logger;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.util.Logging;

/**
 *
 *  @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class ArchiveCoordinateService extends GraphService<ArchiveCoordinateModel>
{
    private static final Logger LOG = Logging.get(ArchiveCoordinateService.class);

    public ArchiveCoordinateService(GraphContext context, Class<ArchiveCoordinateModel> type)
    {
        super(context, type);
    }

    /**
     * Returns a single ArchiveCoordinateModel with given G:A:V.
     * @return null if none found; Logs a WARNING if multiple are found.
     */
    public ArchiveCoordinateModel getSingleOrCreate(String groupId, String artifactId, String version){
        ArchiveCoordinateModel archive = findSingle(groupId, artifactId, version);
        if (archive != null)
            return archive;
        else
            return create().setGroupId(groupId).setArtifactId(artifactId).setVersion(version);
    }

    public ArchiveCoordinateModel findSingle(String groupId, String artifactId, String version){
        Iterable<ArchiveCoordinateModel> archives = findByGAV(groupId, artifactId, version);
        Iterator<ArchiveCoordinateModel> it = archives.iterator();
        if (!it.hasNext())
            return null;
        ArchiveCoordinateModel archive = it.next();
        if(it.hasNext())
            LOG.warning(String.format("There are multiple %s's like this: %s:%s:%s",
                    ArchiveCoordinateModel.class.getSimpleName(), groupId, artifactId, version));
        return archive;
    }


    public Iterable<ArchiveCoordinateModel> findByGAV(String groupId, String artifactId, String version)
    {
        final Iterable<ArchiveCoordinateModel> archives = findAllByProperties(
                new String[]{ArchiveCoordinateModel.GROUP_ID, ArchiveCoordinateModel.ARTIFACT_ID, ArchiveCoordinateModel.VERSION},
                new String[]{groupId, artifactId, version}
        );
        return archives;
    }
}
