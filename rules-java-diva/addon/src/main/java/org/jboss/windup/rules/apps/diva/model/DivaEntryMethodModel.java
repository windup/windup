package org.jboss.windup.rules.apps.diva.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;

@TypeValue(DivaEntryMethodModel.TYPE)
public interface DivaEntryMethodModel extends DivaConstraintModel, JavaMethodModel, DivaRestApiModel {

    String TYPE = "DivaEntryMethodModel";

}
