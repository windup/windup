package org.jboss.windup.qs.skiparch.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.ArchiveModel;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeValue(IgnoredArchiveModel.TYPE)
public interface IgnoredArchiveModel extends ArchiveModel
{
    public static final String TYPE = "SkipArch:ignoredArchive";
    public static final String CAUSE = "SkipArch:ignoredDueTo";

    @Adjacency(label = CAUSE, direction = Direction.OUT)
    public Iterable<IgnoreCauseModel> getCause();

    @Adjacency(label = CAUSE, direction = Direction.OUT)
    public IgnoredArchiveModel setCause(IgnoreCauseModel vul);
}// class
