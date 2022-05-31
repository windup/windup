package org.jboss.windup.rules.apps.java.scan.ast.annotations;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue(JavaAnnotationTypeValueModel.TYPE)
public interface JavaAnnotationTypeValueModel extends WindupVertexFrame {
    String TYPE = "JavaAnnotationTypeValueModel";

}
