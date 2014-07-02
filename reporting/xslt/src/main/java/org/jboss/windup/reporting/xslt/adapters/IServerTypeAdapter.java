package org.jboss.windup.reporting.xslt.adapters;


import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jboss.loom.recog.IServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class IServerTypeAdapter extends XmlAdapter<String, IServerType> {
    private static final Logger log = LoggerFactory.getLogger( IServerTypeAdapter.class );


    @Override
    public String marshal( IServerType v ) throws Exception {
        if( v == null ) return null;
        return v.getDescription();
    }

    /*
    @XmlRootElement
    @Deprecated // Not used in the end, String is enough.
    public static class ServerTypeBean {
        public String typeDescription;
        public String description;
        public ServerTypeBean( String typeDescription, String description ) {
            this.typeDescription = typeDescription;
            this.description = description;
        }
    }*/

    
    @Override
    public IServerType unmarshal( String v ) throws Exception {
        throw new UnsupportedOperationException( "Unmarshall not supported." );
    }

    
}// class
