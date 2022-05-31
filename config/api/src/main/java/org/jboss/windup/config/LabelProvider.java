package org.jboss.windup.config;

import org.jboss.windup.config.metadata.Label;
import org.jboss.windup.config.metadata.LabelProviderData;
import org.jboss.windup.config.metadata.LabelProviderMetadata;

/**
 * An instance of {@link LabelProvider} should represent a single 'LabelSet'. A 'LabelSet' is composed by 'Metadata' and 'Data'; 'Metadata' represents
 * information about the 'LabelSet' like ID, and description; 'Data' represents the information inside a 'LabelSet', for instance the list of all
 * {@link Label}s inside the current 'LabelSet'.
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public interface LabelProvider {
    LabelProviderMetadata getMetadata();

    LabelProviderData getData();
}
