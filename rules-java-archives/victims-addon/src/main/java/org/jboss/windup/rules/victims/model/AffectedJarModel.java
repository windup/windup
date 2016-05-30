package org.jboss.windup.rules.victims.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;

/**
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
@TypeValue(AffectedJarModel.TYPE)
public interface AffectedJarModel extends JarArchiveModel
{
    public static final String TYPE = "victims:affectedJar";
    public static final String VULN = "victims:affectedBy";

    /**
     * The CVE vulnerabilities this archive is affected by.
     */
    @Adjacency(label = VULN, direction = Direction.OUT)
    public Iterable<VulnerabilityModel> getVulnerabilities();

    /**
     * Adds a CVE vulnerability this archive is affected by.
     */
    @Adjacency(label = VULN, direction = Direction.OUT)
    public AffectedJarModel addVulnerability(VulnerabilityModel vul);
}
