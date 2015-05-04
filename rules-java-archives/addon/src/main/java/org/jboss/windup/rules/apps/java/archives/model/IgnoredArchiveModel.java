package org.jboss.windup.rules.apps.java.archives.model;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.IgnoredResourceModel;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@TypeValue(IgnoredArchiveModel.TYPE)
public interface IgnoredArchiveModel extends ArchiveModel, IgnoredResourceModel
{
    public static final String TYPE = "ignoredArchive:";
}
