package org.jboss.windup.rules.apps.diva.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

@TypeValue(DivaSqlOpModel.TYPE)
public interface DivaSqlOpModel extends DivaOpModel {

    String TYPE = "DivaSqlOpModel";
    String SQL = "sql";

    @Property(SQL)
    String getSql();

    @Property(SQL)
    void setSql(String sql);

}
