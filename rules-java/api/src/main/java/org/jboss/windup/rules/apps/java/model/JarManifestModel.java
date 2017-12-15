package org.jboss.windup.rules.apps.java.model;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import com.tinkerpop.blueprints.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains information from the META-INF/MANIFEST.MF file within an archive.
 */
@TypeValue(JarManifestModel.TYPE)
public interface JarManifestModel extends FileModel, SourceFileModel
{
    String TYPE = "JarManifestModel";
    String ARCHIVE = TYPE + "-archiveToManifest";

    @Adjacency(label = ARCHIVE, direction = Direction.IN)
    ArchiveModel getArchive();

    @Adjacency(label = ARCHIVE, direction = Direction.IN)
    void setArchive(final ArchiveModel archive);

    @JavaHandler
    String getName();

    @JavaHandler
    String getVendor();

    @JavaHandler
    String getVersion();

    @JavaHandler
    String getDescription();

    abstract class Impl implements JarManifestModel, JavaHandlerContext<Vertex>
    {
        private static final String SPEC_TITLE = "Specification-Title";
        private static final String BUNDLE_NAME = "Bundle-Name";
        private static final String IMPLEMENTATION_TITLE = "Implementation-Title";

        private static final String BUNDLE_DESCRIPTION = "Bundle-Description";

        private static final String SPEC_VENDOR = "Specification-Vendor";
        private static final String BUNDLE_VENDOR = "Bundle-Vendor";

        private static final String IMPLEMENTATION_VERSION = "Implementation-Version";

        @Override
        public String getName()
        {
            String name = StringUtils.defaultIfBlank((String)it().getProperty(SPEC_TITLE), (String)it().getProperty(BUNDLE_NAME));
            return StringUtils.defaultIfBlank(name, (String)it().getProperty(IMPLEMENTATION_TITLE));
        }

        @JavaHandler
        public String getVendor()
        {
            return StringUtils.defaultIfBlank((String)it().getProperty(SPEC_VENDOR), (String)it().getProperty(BUNDLE_VENDOR));
        }

        @Override
        public String getVersion()
        {
            return (String)it().getProperty(IMPLEMENTATION_VERSION);
        }

        @Override
        public String getDescription()
        {
            return (String)it().getProperty(BUNDLE_DESCRIPTION);
        }
    }
}
