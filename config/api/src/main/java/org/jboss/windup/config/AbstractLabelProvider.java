package org.jboss.windup.config;

import org.jboss.windup.config.metadata.LabelProviderData;
import org.jboss.windup.config.metadata.LabelProviderMetadata;
import org.ocpsoft.rewrite.context.ContextBase;

public abstract class AbstractLabelProvider extends ContextBase implements LabelProvider
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

    @Override
    public boolean equals(Object other)
    {
        boolean result = false;
        if (other instanceof AbstractLabelProvider)
        {
            AbstractLabelProvider that = (AbstractLabelProvider) other;
            result = this.getMetadata().equals(that.getMetadata());
        }
        return result;
    }

    @Override
    public int hashCode()
    {
        return getMetadata().hashCode();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(metadata.getID());

        if (!metadata.getID().equals(metadata.getOrigin())) {
            builder.append(" from ").append(metadata.getOrigin());
        }

        return builder.toString();
    }
}
