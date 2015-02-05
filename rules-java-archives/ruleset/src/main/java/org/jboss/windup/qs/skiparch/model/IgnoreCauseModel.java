package org.jboss.windup.qs.skiparch.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.util.List;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeValue(IgnoreCauseModel.TYPE)
public interface IgnoreCauseModel extends WindupVertexFrame
{
    static final String TYPE = "SkipArch:IgnoreCause";
    static final String LABEL_IGNORES = IgnoredArchiveModel.CAUSE;
    static final String DESC = "SkipArch:desc";


    // CVE. Link: "http://cve.mitre.org/cgi-bin/cvename.cgi?name=" + CVE-ID
    @Property(DESC)
    public String getDescription();

    @Property(DESC)
    public IgnoreCauseModel setDescription(String cve);


    @Adjacency(label = LABEL_IGNORES, direction = Direction.IN)
    public List<IgnoredArchiveModel> getIgnoredArchives();

    @Adjacency(label = LABEL_IGNORES, direction = Direction.IN)
    public IgnoreCauseModel addArchives(IgnoredArchiveModel ignoredArchive);

}// class
