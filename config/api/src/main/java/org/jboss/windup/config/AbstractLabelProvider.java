package org.jboss.windup.config;

import org.jboss.windup.config.metadata.LabelProviderData;
import org.jboss.windup.config.metadata.LabelProviderMetadata;
import org.ocpsoft.rewrite.context.ContextBase;

public class AbstractLabelProvider extends ContextBase implements LabelProvider
{
    private final LabelProviderMetadata metadata;
    private final LabelProviderData data;

    public AbstractLabelProvider(LabelProviderMetadata metadata, LabelProviderData data){
        this.metadata = metadata;
        this.data = data;
    }

    @Override
    public LabelProviderMetadata getMetadata() {
        return metadata;
    }

    @Override
    public LabelProviderData getData() {
        return data;
    }

}
