package org.jboss.windup.reporting.xslt.adapters;


import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ToHashCodeAdapterList extends XmlAdapter<List<String>, List<Object>>{

    @Override
    public List<String> marshal( List<Object> v ) throws Exception {
        if( v == null) return null;
        List<String> ret = new ArrayList( v.size() );
        for( Object object : v) {
            ret.add( object == null ? "" : Integer.toHexString(object.hashCode()));
        }
        return ret;
    }

    @Override
    public List<Object> unmarshal( List<String> v ) throws Exception {
        throw new UnsupportedOperationException("Not supported. Converts objects to their hashcode.");
    }

}// class
