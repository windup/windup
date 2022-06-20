package org.jboss.windup.rules.apps.mavenize;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;

import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.versions.ComparableVersion;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.util.Logging;


/**
 * Serves only to identify the unique singleton object in the DB.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
@TypeValue(GlobalBomModel.TYPE)
public interface GlobalBomModel extends PomXmlModel {
    Logger LOG = Logging.get(GlobalBomModel.class);

    String TYPE = "GlobalBomModel";
    String DEFINES = TYPE + "-defines";

    /**
     * Adds a dependency to BOM. If the BOM already contains that G:A:C:P, sets the dependency version to the newer one.
     */
    @Adjacency(label = DEFINES, direction = Direction.OUT)
    default void addNewerDependency(ArchiveCoordinateModel dependency) {
        LOG.info("Adding: " + dependency.toString());
        final Iterable<ArchiveCoordinateModel> existingDeps = this.getDependencies();
        for (ArchiveCoordinateModel dep : existingDeps) {
            LOG.info("  ---- Dep: " + dep.toString());
            if (!StringUtils.equals(dependency.getGroupId(), dep.getGroupId()))
                continue;
            if (!StringUtils.equals(dependency.getArtifactId(), dep.getArtifactId()))
                continue;
            if (!StringUtils.equals(dependency.getClassifier(), dep.getClassifier()))
                continue;
            if (!StringUtils.equals(dependency.getPackaging(), dep.getPackaging()))
                continue;
            if (0 < compareVersions(dependency.getVersion(), dep.getVersion()))
                dep.setVersion(dependency.getVersion());
            return;
        }
        this.addDependency(dependency);
        LOG.info("Added: " + dependency.toString());
    }

    static int compareVersions(String aS, String bS) {
        ComparableVersion a = new ComparableVersion(aS);
        ComparableVersion b = new ComparableVersion(bS);
        return b.compareTo(a);
    }

}
