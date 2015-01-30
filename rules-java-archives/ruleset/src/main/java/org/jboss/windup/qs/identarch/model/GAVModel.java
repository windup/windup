package org.jboss.windup.qs.identarch.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Maven Artifact - only G:A:V:C.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeValue(GAVModel.TYPE)
public interface GAVModel extends WindupVertexFrame
{
    static final String PREFIX = "identarch:";
    static final String TYPE = PREFIX + "GAVModel";

    @Property(PREFIX + "g")
    String getGroupId();
    @Property(PREFIX + "g")
    GAVModel setGroupId(String groupId);

    @Property(PREFIX + "a")
    String getArtifactId();
    @Property(PREFIX + "a")
    GAVModel setArtifactId(String artifactId);

    @Property(PREFIX + "c")
    String getClassifier();
    @Property(PREFIX + "c")
    GAVModel setClassifier(String classifier);

    @Property(PREFIX + "v")
    String getVersion();
    @Property(PREFIX + "v")
    GAVModel setVersion(String version);

    @Property(PREFIX + "sha1")
    String getSha1();
    @Property(PREFIX + "sha1")
    GAVModel setSha1(String sha1);
}
