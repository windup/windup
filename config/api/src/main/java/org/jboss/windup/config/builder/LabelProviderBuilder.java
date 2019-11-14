package org.jboss.windup.config.builder;

import org.jboss.windup.config.AbstractLabelProvider;
import org.jboss.windup.config.metadata.LabelProviderData;
import org.jboss.windup.config.metadata.LabelProviderMetadata;
import org.jboss.windup.config.LabelProvider;

/**
 * Simple concrete class which receives preexisted 'Metadata' and 'Data' to create a {@link LabelProvider}
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public final class LabelProviderBuilder extends AbstractLabelProvider
{

    public LabelProviderBuilder(LabelProviderMetadata metadata, LabelProviderData data)
    {
        super(metadata, data);
    }

}
