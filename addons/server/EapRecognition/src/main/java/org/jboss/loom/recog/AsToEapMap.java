package org.jboss.loom.recog;


import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Relation between AS and EAP versions.
 *  
 *  TODO: Split into IProjectAnd... impls in .as5 and .as7.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class AsToEapMap implements IProjectAndProductVersionBidiMapper {
    private static final Logger log = LoggerFactory.getLogger( AsToEapMap.class );

    private static final BidiMap map = new DualHashBidiMap();
    
    static {
        
        // AS 7 <-> EAP 6
        map.put("7.2.0.Final", "6.1.0");
        map.put("7.1.3.Final", "6.0.1");
        map.put("7.1.2.Final", "6.0.0");
        
        // AS 5 <-> EAP 5
        map.put("5.2.0.GA", "5.2.0");
        map.put("5.1.0.GA", "5.1.2");
        map.put("5.1.0.GA", "5.1.1");
        map.put("5.1.0.GA", "5.1.0");
        map.put("5.1.0.GA", "5.0.1");
        map.put("5.1.0.GA", "5.0.0");
        
        // Source: https://access.redhat.com/site/articles/112673
    }
    
    @Override public String getProjectToProductVersion(String ver){
        return (String) map.get( ver );
    }

    @Override public String getProductToProjectVersion(String ver){
        return (String) map.getKey( ver );
    }

}// class
