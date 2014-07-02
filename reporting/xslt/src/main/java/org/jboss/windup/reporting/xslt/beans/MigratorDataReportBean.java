package org.jboss.windup.reporting.xslt.beans;


import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.migrators.HasProperties;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.IMigrator;
import org.jboss.windup.reporting.xslt.api.ann.ConfigPartDescriptor;
import org.jboss.windup.reporting.xslt.adapters.IConfigFragmentAdapter;
import org.jboss.windup.reporting.xslt.adapters.MapPropertiesAdapter;

/**
 *  TODO: Create a (static) dictionary of classes -> annotations,
 *        reflect that in XML using elements with IDs, and refer to them by XML REFID;
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlAccessorType( XmlAccessType.NONE )
public class MigratorDataReportBean {

    private ConfigPartDescriptor annotation;
    
    @XmlAttribute
    private Class<? extends IMigrator> fromMigrator;
    
    @XmlElementWrapper(name = "configFragments")
    @XmlElement(name = "configFragment")
    @XmlJavaTypeAdapter(value = IConfigFragmentAdapter.class)
    private List<IConfigFragment> configFragments;
    
    @XmlElement
    private Origin origin;
    
    //@XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    @XmlJavaTypeAdapter(value = MapPropertiesAdapter.class)
    private Map<String, String> properties;
    
    
    /**
     * Constructor, kind of.
     */
    public static MigratorDataReportBean from( MigratorData migData ) {
        MigratorDataReportBean bean = new MigratorDataReportBean();
        
        bean.fromMigrator = migData.getFromMigrator();
        bean.configFragments = migData.getConfigFragments();
        
        // Annotations.
        // From the Data class; or from the IMigrator impl.
        bean.annotation = migData.getClass().getAnnotation( ConfigPartDescriptor.class );
        if( bean.annotation == null ){
            bean.annotation = migData.getFromMigrator().getAnnotation( ConfigPartDescriptor.class );
        }
        
        // Mix-ins
        if( migData instanceof Origin.Wise ){
            bean.origin = ((Origin.Wise)migData).getOrigin();
        }
        
        if( migData instanceof HasProperties ){
            bean.properties = ((HasProperties)migData).getProperties();
        }
        
        return bean;
    }
    
    //private static Map<Class, String> classToNameMap = new HashMap(); // TODO
    


    public Class<? extends IMigrator> getFromMigrator() { return fromMigrator; }
    public List<IConfigFragment> getConfigFragments() { return configFragments; }
    public Origin getOrigin() { return origin; }
    public Map<String, String> getProperties() { return properties; }
    
    
    /**  Derived from an annotation. */
    @XmlAttribute
    public String getName(){ return this.annotation == null ? null : nullIfEmpty( this.annotation.name() ); }
    @XmlAttribute
    public String getDocLink(){ return this.annotation == null ? null : nullIfEmpty( this.annotation.docLink() ); }
    @XmlAttribute
    public String getIconFile(){ return this.annotation == null ? null : nullIfEmpty( this.annotation.iconFile() ); }
    
    
    private static String nullIfEmpty(String str){ return str == null ? null : (str.isEmpty() ? null : str); }

}// class
