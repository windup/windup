package org.jboss.windup.rules.apps.java.scan.ast.annotations;

import org.jboss.windup.reporting.model.WindupVertexListModel;

import org.jboss.windup.graph.model.TypeValue;

@TypeValue(JavaAnnotationListTypeValueModel.TYPE)
public interface JavaAnnotationListTypeValueModel extends JavaAnnotationTypeValueModel, WindupVertexListModel<JavaAnnotationTypeValueModel> {
    public static final String TYPE = "JavaAnnotationListTypeValueModel";

}
