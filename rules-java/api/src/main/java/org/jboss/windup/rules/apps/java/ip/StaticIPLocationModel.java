package org.jboss.windup.rules.apps.java.ip;

import org.jboss.windup.reporting.model.InlineHintModel;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains the location of a hard-coded IP address within a file
 *
 */
@TypeValue(StaticIPLocationModel.TYPE)
public interface StaticIPLocationModel extends InlineHintModel
{
    String TYPE = "StaticIPLocationModel";
}
