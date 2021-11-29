package org.jboss.windup.rules.apps.diva.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue(DivaRequestParamModel.TYPE)
public interface DivaRequestParamModel extends WindupVertexFrame {

    String TYPE = "DivaRequestParamModel";
    String PARAM_NAME = "paramName";
    String PARAM_VALUE = "paramValue";

    @Property(PARAM_NAME)
    String getParamName();

    @Property(PARAM_NAME)
    void setParamName(String name);

    @Property(PARAM_VALUE)
    String getParamValue();

    @Property(PARAM_VALUE)
    void setParamValue(String value);

}
