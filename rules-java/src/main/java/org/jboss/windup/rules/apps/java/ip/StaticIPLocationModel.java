package org.jboss.windup.rules.apps.java.ip;

import org.jboss.windup.reporting.model.InlineHintModel;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains a Java package name
 *
 */
@TypeValue(StaticIPLocationModel.TYPE)
public interface StaticIPLocationModel extends InlineHintModel
{
	String TYPE = "StaticIPLocationModel";
}
