package org.jboss.windup.reporting.xslt.model;

import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.jboss.windup.graph.model.ApplicationArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;

/**
 *  Root JAXB bean for the XML report.
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name="migrationReport")
@XmlAccessorType( XmlAccessType.NONE )
public class MigrationReportJaxbBean {

    @XmlElement
    public WindupConfigurationModel config;
    

    
    @XmlElementWrapper(name = "deployments")
    @XmlElement(name = "deployment")
    public Collection<ApplicationArchiveModel> deployments;
    
}// class
