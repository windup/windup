package org.jboss.windup.reporting.xslt.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.reporting.xslt.api.ann.Property;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlAccessorType( value = XmlAccessType.NONE )
public final class ReportProperty {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger( ReportProperty.class );

    
    @XmlAttribute
    public String name;
    
    @XmlAttribute
    public String label;
    
    @XmlAttribute
    public String value;
    
    @XmlAttribute 
    public String style;


    public ReportProperty() {
    }


    public ReportProperty( String name, String value ) {
        this.name = name;
        this.value = value;
    }


    public ReportProperty setStyle( String style ) { this.style = style; return this; }
    public ReportProperty setLabel( String label ) { this.label = label; return this; }
    

    
    public static List<ReportProperty> extractReportProperties( Object obj ) {
        if( obj == null )  return null;
        
        List<ReportProperty> props = new LinkedList();
        for( Method method : obj.getClass().getMethods() ) {
            if( method.getParameterTypes().length != 0 ) continue;
            Property ann = method.getAnnotation( Property.class );
            if( null == ann )  continue;
            //if( ! method.getName().startsWith("get") ) // Relax.
            Object val;
            try {
                val = method.invoke( obj );
            } catch(     IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) {
                log.error("Failed invoking method " + method.getName() + " on " + obj + ":\n    " + ex.getMessage(), ex);                val = null;
            }
            
            String propName = StringUtils.removeStart( method.getName(), "get");
            props.add(
                new ReportProperty( propName, "" + val )
                    .setStyle(ann.style())
                    .setLabel(ann.label()) 
            );
        }
        //return props.toArray(new ReportProperty[props.size()]);
        return props;
    }
    
}// class
