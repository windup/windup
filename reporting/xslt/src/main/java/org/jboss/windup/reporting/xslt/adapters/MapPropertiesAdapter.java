package org.jboss.windup.reporting.xslt.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jboss.windup.reporting.xslt.beans.Properties;
import org.jboss.windup.reporting.xslt.beans.ReportProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MapPropertiesAdapter extends XmlAdapter<Properties, Map<String,String>> {
    private static final Logger log = LoggerFactory.getLogger( MapPropertiesAdapter.class );

    @Override public Properties marshal( Map<String, String> map ) throws Exception {
        if( map == null )
            return null;
        
        List<ReportProperty> ret = new ArrayList( map.size() );
        for( Map.Entry<String, String> entry : map.entrySet() ) {
            ret.add( new ReportProperty( entry.getKey(), entry.getValue() ) );
        }
        return new Properties( ret );
    }
    
    
    @Override public Map<String, String> unmarshal( Properties v ) throws Exception {
        throw new UnsupportedOperationException( "Not supported." );
    }
    
}// class
