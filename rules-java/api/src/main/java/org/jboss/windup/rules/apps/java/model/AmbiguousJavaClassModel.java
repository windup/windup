package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.TypeValue;

@TypeValue(AmbiguousJavaClassModel.TYPE)
public interface AmbiguousJavaClassModel extends AmbiguousReferenceModel<JavaClassModel>, JavaClassModel {
    String TYPE = "AmbiguousJavaClassModel";
}
