package org.jboss.windup.rules.apps.diva.model;

import org.jboss.windup.graph.model.TypeValue;

@TypeValue(DivaRequestConstraintModel.TYPE)
public interface DivaRequestConstraintModel extends DivaRequestParamModel, DivaConstraintModel {

    String TYPE = "DivaRequestConstraintModel";

}
