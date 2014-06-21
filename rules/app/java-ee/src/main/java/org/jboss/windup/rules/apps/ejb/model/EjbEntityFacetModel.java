package org.jboss.windup.rules.apps.ejb.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.reporting.renderer.api.Label;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassMetaModel;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;

@TypeValue("EJBEntityFacet")
public interface EjbEntityFacetModel extends JavaClassMetaModel
{
    @Label
    @Property("ejbEntityName")
    public String getEjbEntityName();

    @Property("ejbEntityName")
    public String setEjbEntityName(String ejbEntityName);

    @Property("displayName")
    public String getDisplayName();

    @Property("displayName")
    public void setDisplayName(String displayName);

    @Property("ejbId")
    public String getEjbId();

    @Property("ejbId")
    public void setEjbId(String id);

    @Property("persistenceType")
    public String getPersistenceType();

    @Property("persistenceType")
    public void setPersistenceType(String PersistenceType);

    @Adjacency(label = "ejbLocal", direction = Direction.OUT)
    public void setEjbLocal(JavaClassModel ejbLocal);

    @Adjacency(label = "ejbLocal", direction = Direction.OUT)
    public JavaClassModel getEjbLocal();

    @Adjacency(label = "ejbLocalHome", direction = Direction.OUT)
    public void setEjbLocalHome(JavaClassModel ejbLocalHome);

    @Adjacency(label = "ejbLocalHome", direction = Direction.OUT)
    public JavaClassModel getEjbLocalHome();
}
