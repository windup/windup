package org.jboss.windup.rules.victims.model;


import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.reporting.model.ApplicationReportModel;

/**
 * Root model for Victims report.
 *
 * @author Ondrej Zizka
 */
@TypeValue(VictimsReportModel.TYPE)
public interface VictimsReportModel extends ApplicationReportModel
{
    public static final String TYPE = "victims:report";
    public static final String AFFECTED_JARS = "affectedJars";

    /**
     * Jars affected by a vulnerability.
     */
    @Adjacency(label = AFFECTED_JARS, direction = Direction.OUT)
    public Iterable<AffectedJarModel> getAffectedJars();

    /**
     * Add a jar affected by a vulnerability.
     */
    @Adjacency(label = AFFECTED_JARS, direction = Direction.OUT)
    public void addAffectedJar(AffectedJarModel jar);
}
