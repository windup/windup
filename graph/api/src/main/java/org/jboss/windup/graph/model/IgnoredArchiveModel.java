package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.IgnoredFileModel;

/**
 * Indicates that an Archive file can be ignored.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@TypeValue(IgnoredArchiveModel.TYPE)
public interface IgnoredArchiveModel extends ArchiveModel, IgnoredFileModel
{
    String TYPE = "IgnoredArchiveModel";
}
