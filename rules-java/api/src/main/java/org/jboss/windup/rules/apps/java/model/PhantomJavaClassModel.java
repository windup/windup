package org.jboss.windup.rules.apps.java.model;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * A {@link JavaClassModel} that has not yet been found. We have a reference to it,
 * but we have not yet found an actual ".class" or ".java" file with the code.
 */
@TypeValue("PhantomJavaClass")
public interface PhantomJavaClassModel extends JavaClassModel
{

}
