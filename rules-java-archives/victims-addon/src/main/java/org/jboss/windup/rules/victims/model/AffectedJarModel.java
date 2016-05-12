package org.jboss.windup.rules.victims.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeValue(AffectedJarModel.TYPE)
public interface AffectedJarModel extends JarArchiveModel
{
    public static final String TYPE = "victims:affectedJar";
    public static final String VULN = "victims:affectedBy";

    @Adjacency(label = VULN, direction = Direction.OUT)
    public Iterable<VulnerabilityModel> getVulnerabilities();

    @Adjacency(label = VULN, direction = Direction.OUT)
    public AffectedJarModel addVulnerability(VulnerabilityModel vul);
}// class
