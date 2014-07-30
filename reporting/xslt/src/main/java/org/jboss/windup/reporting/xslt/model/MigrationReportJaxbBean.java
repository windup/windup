package org.jboss.windup.reporting.xslt.model;

import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.reporting.xslt.adapters.MigratorDataSubtypesAdapter;
import org.jboss.windup.reporting.xslt.adapters.ToActionBeanAdapter;
import org.jboss.windup.reporting.xslt.adapters.ToStringAdapter;

/**
 *  Root JAXB bean for the XML report.
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name="migrationReport")
@XmlAccessorType( XmlAccessType.NONE )
public class MigrationReportJaxbBean {

    @XmlElement
    public WindupConfigurationModel config;
    
    @XmlElement(name = "sourceServer", required = true)
    public ServerInfo sourceServer;
    
    @XmlElement
    public ComparisonResult comparisonResult;
    
    @XmlElementWrapper(name = "configsData")
    @XmlElement(name = "configData")
    @XmlJavaTypeAdapter( MigratorDataSubtypesAdapter.class )
    public Collection<MigratorData> configData;
    
    @XmlElementWrapper(name = "deployments")
    @XmlElement(name = "deployment")
    public Collection<DeploymentInfo> deployments;

    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "action")
    @XmlJavaTypeAdapter( ToActionBeanAdapter.class )
    public List<IMigrationAction> actions;
    
    @XmlElement(name="finalException")
    @XmlJavaTypeAdapter( ToStringAdapter.class )
    public WindupException finalException;

}// class
