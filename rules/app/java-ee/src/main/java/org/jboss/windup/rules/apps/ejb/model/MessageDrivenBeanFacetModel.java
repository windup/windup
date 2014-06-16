package org.jboss.windup.rules.apps.ejb.model;

import org.jboss.windup.rules.apps.java.scan.model.JavaClassMetaModel;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("MessageDrivenBeanFacet")
public interface MessageDrivenBeanFacetModel extends JavaClassMetaModel
{

    @Label
    @Property("messageDrivenBeanName")
    public String getMessageDrivenBeanName();

    @Property("messageDrivenBeanName")
    public String setMessageDrivenBeanName(String messageDrivenBeanName);

    @Property("displayName")
    public String getDisplayName();

    @Property("displayName")
    public void setDisplayName(String displayName);

    @Property("ejbId")
    public String getEjbId();

    @Property("ejbId")
    public void setEjbId(String id);

    @Property("transactionType")
    public String getTransactionType();

    @Property("transactionType")
    public void setTransactionType(String transactionType);

    @Property("sessionType")
    public String getSessionType();

    @Property("sessionType")
    public void setSessionType(String sessionType);
}
