package org.jboss.windup.rules.apps.diva.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue(DivaRestApiModel.TYPE)
public interface DivaRestApiModel extends WindupVertexFrame {
    String TYPE = "DivaRestApiModel";
    String URL_PATH = "urlPath";
    String HTTP_MEHOD = "httpMethod";

    @Property(HTTP_MEHOD)
    String getHttpMethod();

    @Property(HTTP_MEHOD)
    void setHttpMethod(String method);

    @Property(URL_PATH)
    String getUrlPath();

    @Property(URL_PATH)
    void setUrlPath(String path);
}
