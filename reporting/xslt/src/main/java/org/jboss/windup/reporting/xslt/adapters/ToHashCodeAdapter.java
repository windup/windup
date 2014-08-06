package org.jboss.windup.reporting.xslt.adapters;


import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ToHashCodeAdapter extends XmlAdapter<String, Object>{

    @Override
    public String marshal( Object v ) throws Exception {
        if( v == null )  return "";
        return Integer.toHexString(v.hashCode());
    }

    @Override
    public Object unmarshal( String v ) throws Exception {
        throw new UnsupportedOperationException("Not supported. Converts objects to their hashcode.");
    }
    
}// class
