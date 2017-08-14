package org.jboss.windup.rules.apps.java.model;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.apache.commons.lang.StringUtils;

/**
 * Represents a source ".java" file on disk.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(JavaSourceFileModel.TYPE)
public interface JavaSourceFileModel extends AbstractJavaSourceModel
{
    ///String UNPARSEABLE_JAVA_CLASSIFICATION = "Unparseable Java File";
    ///String UNPARSEABLE_JAVA_DESCRIPTION = "This Java file could not be parsed";

    String TYPE = "JavaSourceFileModel";

    abstract class Impl implements JavaSourceFileModel, JavaHandlerContext<Vertex>
    {
        @Override
        public String getPrettyPathWithinProject(boolean useFQNForClasses)
        {
            if (!useFQNForClasses) {
                return this.getPrettyPathWithinProject();
            }

            String filename = StringUtils.removeEndIgnoreCase(getFileName(), ".java");
            String packageName = getPackageName();

            return (packageName == null || packageName.isEmpty()) ? filename : packageName + "." + filename;
        }
    }

}
