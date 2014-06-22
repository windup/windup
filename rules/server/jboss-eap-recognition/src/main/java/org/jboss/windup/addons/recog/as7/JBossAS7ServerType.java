package org.jboss.windup.addons.recog.as7;


import java.io.File;
import org.jboss.windup.addons.recog.IServerType;
import org.jboss.windup.addons.recog.VersionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class JBossAS7ServerType implements IServerType {
    private static final Logger log = LoggerFactory.getLogger( JBossAS7ServerType.class ); 
    
    @Override public String getDescription() { return "JBoss AS 7+ or JBoss EAP 6+"; }


    @Override
    public VersionRange recognizeVersion( File homeDir ) {
        if( isPresentInDir( homeDir ) )
            return new VersionRange( "7.0.0", null );
        return new VersionRange();
    }


    @Override
    public boolean isPresentInDir( File homeDir ) {
        if( ! new File(homeDir, "jboss-modules.jar").exists() )
            return false;
        if( ! new File(homeDir, "standalone/configuration").exists() )
            return false;
        if( ! new File(homeDir, "bin/standalone.sh").exists() )
            return false;
        
        return true;
    }


    @Override @SuppressWarnings("deprecation")
    public String format( VersionRange versionRange ) {
        StringBuilder sb = new StringBuilder("JBoss ");
        
        // Version unknown
        if( versionRange == null || versionRange.from == null )
            return sb.append("AS 7 or EAP 6, or WildFly 8").toString();
        
        // AS or EAP?
        sb.append( versionRange.from.verProduct == null ? "AS " : "EAP ");
        sb.append( versionRange.getFrom_preferProduct() );
        
        // Range?
        if( versionRange.to == null )
            return sb.toString();
        
        sb.append(" - ").append( versionRange.getTo_preferProduct() );
        return sb.toString();
    }

}// class
