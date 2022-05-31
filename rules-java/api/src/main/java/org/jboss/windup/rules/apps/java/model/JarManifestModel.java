package org.jboss.windup.rules.apps.java.model;

import com.syncleus.ferma.ElementFrame;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

/**
 * Contains information from the META-INF/MANIFEST.MF file within an archive.
 */
@TypeValue(JarManifestModel.TYPE)
public interface JarManifestModel extends FileModel, SourceFileModel, ElementFrame {
    String TYPE = "JarManifestModel";
    String ARCHIVE = TYPE + "-archiveToManifest";

    String SPEC_TITLE = "Specification-Title";
    String BUNDLE_NAME = "Bundle-Name";
    String IMPLEMENTATION_TITLE = "Implementation-Title";

    String BUNDLE_DESCRIPTION = "Bundle-Description";

    String SPEC_VENDOR = "Specification-Vendor";
    String BUNDLE_VENDOR = "Bundle-Vendor";

    String IMPLEMENTATION_VERSION = "Implementation-Version";

    @Adjacency(label = ARCHIVE, direction = Direction.IN)
    ArchiveModel getArchive();

    @Adjacency(label = ARCHIVE, direction = Direction.IN)
    void setArchive(final ArchiveModel archive);

    default String getName() {
        String name = StringUtils.defaultIfBlank(getProperty(SPEC_TITLE), getProperty(BUNDLE_NAME));
        return StringUtils.defaultIfBlank(name, getProperty(IMPLEMENTATION_TITLE));
    }

    default String getVendor() {
        return StringUtils.defaultIfBlank(getProperty(SPEC_VENDOR), getProperty(BUNDLE_VENDOR));
    }

    default String getVersion() {
        return getProperty(IMPLEMENTATION_VERSION);
    }

    default String getDescription() {
        return getProperty(BUNDLE_DESCRIPTION);
    }

}
