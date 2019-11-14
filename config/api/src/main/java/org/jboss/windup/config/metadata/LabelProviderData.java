package org.jboss.windup.config.metadata;

import java.util.List;

/**
 * This {@link LabelProviderData} should keep the 'Data' inside a 'LabelSet', for instance the list of {@link Label}s
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 *
 */
public interface LabelProviderData
{
    /**
     * Return the list of {@Link Label}s inside a 'Labelset'
     */
    List<Label> getLabels();
}
