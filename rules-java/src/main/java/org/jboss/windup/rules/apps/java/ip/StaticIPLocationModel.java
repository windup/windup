package org.jboss.windup.rules.apps.java.ip;

import org.jboss.windup.rules.files.model.FileLocationModel;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains a Java package name
 *
 */
@TypeValue(StaticIPLocationModel.TYPE)
public interface StaticIPLocationModel extends FileLocationModel
{
	String TYPE = "StaticIPLocationModel";
}
