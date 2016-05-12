package org.jboss.windup.rules.apps.java.archives.model;

import com.tinkerpop.blueprints.Vertex;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.apache.commons.lang.StringUtils;

/**
 * Represents a {@link Coordinate} for an {@link IdentifiedArchiveModel}.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@TypeValue(ArchiveCoordinateModel.TYPE)
public interface ArchiveCoordinateModel extends WindupVertexFrame
{
    public static final String TYPE = "coordinate:";
    public static final String GROUP_ID    = TYPE + "groupId";
    public static final String ARTIFACT_ID = TYPE + "artifactId";
    public static final String PACKAGING   = TYPE + "packaging";
    public static final String CLASSIFIER  = TYPE + "classifier";
    public static final String VERSION     = TYPE + "version";


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
    @JavaHandler
    String toString();

    public abstract class Impl implements ArchiveCoordinateModel, JavaHandlerContext<Vertex>
    {
        public String toString(){
            return String.format("%s:%s:%s:%s:%s", this.getGroupId(), this.getArtifactId(), this.getVersion(),
                    StringUtils.defaultString(this.getClassifier()), this.getPackaging());
        }
    }
}
