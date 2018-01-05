package org.jboss.windup.rules.apps.java.archives.model;

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.windup.graph.JavaHandler;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.syncleus.ferma.annotations.Property;

/**
 * Represents a {@link Coordinate} for an {@link IdentifiedArchiveModel}.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@TypeValue(ArchiveCoordinateModel.TYPE)
public interface ArchiveCoordinateModel extends WindupVertexFrame
{
    public static final String TYPE = "ArchiveCoordinateModel";
    public static final String GROUP_ID = TYPE + "-groupId";
    public static final String ARTIFACT_ID = TYPE + "-artifactId";
    public static final String PACKAGING = TYPE + "-packaging";
    public static final String CLASSIFIER = TYPE + "-classifier";
    public static final String VERSION = TYPE + "-version";

    @Property(GROUP_ID)
    String getGroupId();

    @Property(GROUP_ID)
    ArchiveCoordinateModel setGroupId(String groupId);

    @Property(ARTIFACT_ID)
    String getArtifactId();

    @Property(ARTIFACT_ID)
    ArchiveCoordinateModel setArtifactId(String artifactId);

    @Property(PACKAGING)
    String getPackaging();

    @Property(PACKAGING)
    ArchiveCoordinateModel setPackaging(String packaging);

    @Property(CLASSIFIER)
    String getClassifier();

    @Property(CLASSIFIER)
    ArchiveCoordinateModel setClassifier(String classifier);

    @Property(VERSION)
    String getVersion();

    @Property(VERSION)
    ArchiveCoordinateModel setVersion(String version);

    /**
     * @return Formatted as "G:A:V:C:P"
     */
    @JavaHandler(handler = Impl.class)
    String toString();

    class Impl
    {
        public String toString(ArchiveCoordinateModel coordinateModel)
        {
            return String.format("%s:%s:%s:%s:%s", coordinateModel.getGroupId(), coordinateModel.getArtifactId(), coordinateModel.getVersion(),
                        StringUtils.defaultString(coordinateModel.getClassifier()), coordinateModel.getPackaging());
        }
    }
}
