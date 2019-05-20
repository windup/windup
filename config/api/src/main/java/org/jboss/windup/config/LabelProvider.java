package org.jboss.windup.config;

import org.jboss.windup.config.metadata.LabelProviderData;
import org.jboss.windup.config.metadata.LabelProviderMetadata;

public interface LabelProvider
{
    LabelProviderMetadata getMetadata();
    LabelProviderData getData();
}
