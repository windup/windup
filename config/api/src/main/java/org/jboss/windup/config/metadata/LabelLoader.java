package org.jboss.windup.config.metadata;

import org.jboss.windup.config.loader.RuleLoaderContext;

import java.util.Collection;

/**
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public interface LabelLoader
{
    Collection<Label> loadLabels(RuleLoaderContext ruleLoaderContext);
}
