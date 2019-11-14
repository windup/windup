package org.jboss.windup.config.metadata;

import org.jboss.windup.config.LabelProvider;

/**
 * Provide the 'Metadata' information of a 'Labelset'
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public interface LabelsetMetadata
{
    /**
     * Returns a unique identifier for the corresponding {@link LabelProvider}.
     */
    String getID();

    /**
     * Returns a descriptive {@link String}, informing a human where they can find the {@link Label} instances.
     */
    String getOrigin();

    /**
     * Returns a human-readable description of the labels associated with this {@link LabelsetMetadata}.
     */
    String getDescription();

    /**
     * This defines the order on which a labelSet will be shown in a report. Lowest priority values will be loaded first. By default core labelSets
     * must have priority 1
     */
    int getPriority();
}
