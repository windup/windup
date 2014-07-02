package org.jboss.windup.reporting.xslt.beans;

import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jboss.loom.migrators.HasProperties;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.windup.reporting.xslt.api.ann.ConfigPartDescriptor;
import org.jboss.windup.reporting.xslt.adapters.MapPropertiesAdapter;
import org.jboss.loom.utils.Utils;
import org.jboss.windup.utils.el.SimpleEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlAccessorType( XmlAccessType.NONE )
public class ConfigFragmentReportBean {
    private static final Logger log = LoggerFactory.getLogger(ConfigFragmentReportBean.class);
    
    
    private ConfigPartDescriptor annotation;
    
    @XmlAttribute(name = "class")
    private Class<? extends IConfigFragment> cls;

    @XmlElement
    private Origin origin;

    //@XmlElementWrapper(name = "properties")
    //@XmlElement(name = "property")
    @XmlJavaTypeAdapter(value = MapPropertiesAdapter.class)
    private Map<String, String> properties;

    
    /**
     * Constructor, kind of.
     */
    public static ConfigFragmentReportBean from( IConfigFragment fragment ) {
        ConfigFragmentReportBean bean = new ConfigFragmentReportBean();
        
        bean.cls = fragment.getClass();
        
        // Annotations.
        bean.annotation = fragment.getClass().getAnnotation( ConfigPartDescriptor.class );
        
        if( fragment instanceof Origin.Wise ){
            bean.origin = ((Origin.Wise)fragment).getOrigin();
        }
        
        // Properties - own.
        if( fragment instanceof HasProperties ){
            bean.properties = ((HasProperties)fragment).getProperties();
        }
        // Properties - getters to a map.
        else {
            /*try {
                bean.properties = BeanUtils.describe( fragment );
            } catch(     IllegalAccessException | InvocationTargetException | NoSuchMethodException ex ) {
                log.warn("Failed extracting properties from " + bean.getClass().getSimpleName() + ":\n    " + ex.getMessage(), ex );
            }*/
            bean.properties = Utils.describeBean( fragment );
        }
        
        return bean;
    }
    
    
    /* Derived from an annotation. TODO: Move to a base class?*/
    @XmlAttribute
    public String getName(){
        String name = this.annotation == null ? null : Utils.nullIfEmpty( this.annotation.name() );
        if( name == null || ! name.contains("${"))  return name;
        if( properties == null || properties.isEmpty() )  return name;
        
        try {
            return new SimpleEvaluator(this.properties).evaluateEL( name );
        } catch (Exception ex ){
            return name;
        }
    }
    
    @XmlAttribute
    public String getDocLink(){ return this.annotation == null ? null : Utils.nullIfEmpty( this.annotation.docLink() ); }
    @XmlAttribute
    public String getIconFile(){ return this.annotation == null ? null : Utils.nullIfEmpty( this.annotation.iconFile() ); }
    

}// class
