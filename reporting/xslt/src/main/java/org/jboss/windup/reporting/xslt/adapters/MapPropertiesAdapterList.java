package org.jboss.windup.reporting.xslt.adapters;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Adapts Map<String, String> to List<MapPropertiesAdapterList.Property>.
 *  Doesn't use java.util.Properties as it works with Objects.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MapPropertiesAdapterList extends XmlAdapter<List<MapPropertiesAdapterList.Property>, Map<String,String>> {
    private static final Logger log = LoggerFactory.getLogger( MapPropertiesAdapterList.class );

    @Override public List<Property> marshal( Map<String, String> map ) throws Exception {
        if( map == null )
            return null;
        
        List<Property> ret = new ArrayList( map.size() );
        for( Map.Entry<String, String> entry : map.entrySet() ) {
            ret.add( new Property( entry.getKey(), entry.getValue() ) );
        }
        return ret;
    }

    
    // Bean
    @XmlRootElement
    @XmlAccessorType( XmlAccessType.NONE )
    public static final class Property {
        
        @XmlAttribute public String name;
        
        @XmlAttribute public String value;

        public Property() {
        }
        
        public Property( String name, String value ) {
            this.name = name;
            this.value = value;
        }
    }

    
    @Override public Map<String, String> unmarshal( List<Property> v ) throws Exception {
        throw new UnsupportedOperationException( "Not supported." );
    }
    
}// class
