package org.jboss.windup.reporting.xslt.beans;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.spi.IMigrator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jboss.windup.reporting.xslt.api.ann.ActionDescriptor;
import org.jboss.windup.reporting.xslt.adapters.ToHashCodeAdapter;

/**
 *   Wraps an action.
 *   Because JAXB needs getters (can't read from just any methods),
 *   Weaving it with JAXB required too much changes to the interface and actions.
 */
@XmlRootElement(name="action")
@XmlAccessorType( XmlAccessType.NONE )
public class ActionBean {


    @XmlAttribute(name = "id") @XmlID
    private String hashCode;
    
    @XmlAttribute(name = "class")
    private Class<? extends IMigrationAction> cls;
    
    @XmlAttribute
    private String label;
    
    @XmlElementWrapper(name="properties")
    @XmlElement(name="property")
    private List<ReportProperty> reportProperties;
    
    @XmlElement(name = "originMsg")
    private String originMessage;
    
    @XmlAttribute(name = "fromMigrator")
    private Class<? extends IMigrator> fromMigrator;
    
    @XmlElementWrapper(name = "dependencies")
    @XmlElement(name = "dep")
    @XmlJavaTypeAdapter( ToHashCodeAdapter.class )
    private List<IMigrationAction> dependencies;
    
    @XmlElement(name = "desc")
    private String description;

    
    @XmlElementWrapper(name="warnings")
    @XmlElement(name="warning")
    private List<String> warnings;


    public ActionBean() { }
    public static ActionBean from( IMigrationAction action ) {
        ActionBean bean = new ActionBean();
        
        bean.cls = action.getClass();
        
        final ActionDescriptor ann = action.getClass().getAnnotation( ActionDescriptor.class );
        if( ann != null ){
            bean.label = ann.header();
        }
        List<ReportProperty> props = ReportProperty.extractReportProperties( action );
        bean.reportProperties = props;
        
        bean.hashCode = Integer.toHexString( action.hashCode() );
        bean.setDescription( action.toDescription() );
        bean.setOriginMessage( action.getOriginMessage() );
        bean.setFromMigrator( action.getFromMigrator() );
        bean.setDependencies( action.getDependencies() );
        bean.setWarnings( action.getWarnings() );
        return bean;
    }


    public String getOriginMessage() { return originMessage; }
    public void setOriginMessage( String originMessage ) { this.originMessage = originMessage; }
    public Class<? extends IMigrator> getFromMigrator() { return fromMigrator; }
    public void setFromMigrator( Class<? extends IMigrator> fromMigrator ) { this.fromMigrator = fromMigrator; }
    public List<IMigrationAction> getDependencies() { return dependencies; }
    public void setDependencies( List<IMigrationAction> dependencies ) { this.dependencies = dependencies; }
    public String getDescription() { return description; }
    public void setDescription( String description ) { this.description = description; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings( List<String> warnings ) { this.warnings = warnings; }
    
}// class
