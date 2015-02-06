package org.jboss.windup.rules.apps.java.archives.model;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@TypeValue(IgnoredArchiveModel.TYPE)
public interface IgnoredArchiveModel extends ArchiveModel, IgnoredFileModel
{
    public static final String TYPE = "ignoredArchive:";
}
