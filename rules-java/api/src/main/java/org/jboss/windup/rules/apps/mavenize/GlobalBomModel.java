package org.jboss.windup.rules.apps.mavenize;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.versions.ComparableVersion;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.util.Logging;


/**
 * Serves only to identify the unique singleton object in the DB.
 */
@TypeValue(GlobalBomModel.TYPE)
public interface GlobalBomModel extends PomXmlModel
{
    String TYPE = "GlobalBom-";
    String DEFINES = TYPE + "defines";

    /**
     * Adds a dependency to BOM. If the BOM already contains that G:A:C:P, sets the dependency version to the newer one.
     */
    @JavaHandler
    @Adjacency(label = DEFINES, direction = Direction.OUT)
    void addNewerDependency(ArchiveCoordinateModel dependency);

    public abstract class Impl implements GlobalBomModel, JavaHandlerContext<Vertex> {
        private static final Logger LOG = Logging.get(MavenizeRuleProvider.class);

        @Override
        public void addNewerDependency(final ArchiveCoordinateModel newCoord){
            LOG.info("Adding: " + newCoord.toString());
            Vertex v = this.it();
            final Iterable<ArchiveCoordinateModel> existingDeps = this.getDependencies();
            for (ArchiveCoordinateModel dep : existingDeps)
            {
                LOG.info("  ---- Dep: " + dep.toString());
                if(!StringUtils.equals(newCoord.getGroupId(), dep.getGroupId()))
                    continue;
                if(!StringUtils.equals(newCoord.getArtifactId(), dep.getArtifactId()))
                    continue;
                if(!StringUtils.equals(newCoord.getClassifier(), dep.getClassifier()))
                    continue;
                if(!StringUtils.equals(newCoord.getPackaging(), dep.getPackaging()))
                    continue;
                if(0 < compareVersions(newCoord.getVersion(), dep.getVersion()))
                    dep.setVersion(newCoord.getVersion());
                return;
            }
            this.addDependency(newCoord);
            LOG.info("Added: " + newCoord.toString());
        }

        /** Uses Forge's comprehension of version strings. */
        private static int compareVersions(String aS, String bS)
        {
            ComparableVersion a = new ComparableVersion(aS);
            ComparableVersion b = new ComparableVersion(bS);
            return b.compareTo(a);
        }
    }
}
