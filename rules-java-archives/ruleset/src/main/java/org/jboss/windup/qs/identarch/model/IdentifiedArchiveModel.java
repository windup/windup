package org.jboss.windup.qs.identarch.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.qs.skiparch.model.IgnoredArchiveModel;

/**
 * An identified archive points to GAVModel which says what this archive is
 * (typically, after being identified by IdentArch ruleset).
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeValue(IdentifiedArchiveModel.TYPE)
public interface IdentifiedArchiveModel extends ArchiveModel
{
    public static final String TYPE = "IdentArch:IdentifiedArchive";
    public static final String IDENT_AS = "IdentArch:identifiedAs";

    @Adjacency(label = IDENT_AS, direction = Direction.OUT)
    public GAVModel getGAV();

    @Adjacency(label = IDENT_AS, direction = Direction.OUT)
    public IgnoredArchiveModel setGAV(GAVModel vul);

}// class
