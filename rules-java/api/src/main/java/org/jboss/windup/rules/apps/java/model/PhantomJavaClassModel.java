package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.TypeValue;

/**
 * A {@link JavaClassModel} that has not yet been found. We have a reference to it,
 * but we have not yet found an actual ".class" or ".java" file with the code.
 */
@TypeValue(PhantomJavaClassModel.TYPE)
public interface PhantomJavaClassModel extends JavaClassModel {
    String TYPE = "PhantomJavaClassModel";
}
