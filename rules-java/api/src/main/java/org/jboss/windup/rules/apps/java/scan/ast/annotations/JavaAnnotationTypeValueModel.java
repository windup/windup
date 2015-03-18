package org.jboss.windup.rules.apps.java.scan.ast.annotations;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(JavaAnnotationTypeValueModel.TYPE)
public interface JavaAnnotationTypeValueModel extends WindupVertexFrame
{
    public static final String TYPE = "JavaAnnotationTypeValue";

}
