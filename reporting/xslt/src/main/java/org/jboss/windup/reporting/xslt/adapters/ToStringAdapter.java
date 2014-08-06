package org.jboss.windup.reporting.xslt.adapters;


import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ToStringAdapter extends XmlAdapter<String, Object> {
    private static final Logger log = LoggerFactory.getLogger( ToStringAdapter.class );

    @Override
    public String marshal( Object v ) throws Exception {
        return v == null ? null : v.toString();
    }
    
    @Override
    public Object unmarshal( String v ) throws Exception {
        throw new UnsupportedOperationException("Not supported. Converts objects using toString().");
    }
    
}// class
