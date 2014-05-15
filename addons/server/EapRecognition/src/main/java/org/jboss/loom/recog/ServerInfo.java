package org.jboss.loom.recog;

import java.io.File;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlWriteOnly;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.utils.compar.ComparisonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  What did we recognize about the server; Currently just type and version.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlAccessorType( XmlAccessType.NONE)
public class ServerInfo {
    private static final Logger log = LoggerFactory.getLogger( ServerInfo.class );

    @XmlAttribute //@XmlJavaTypeAdapter(FileToPathAdapter.class)
    private final File serverRootDir;
    
    //@XmlAttribute @XmlJavaTypeAdapter( IServerTypeAdapter.class )
    private IServerType type = null;
    
    //@XmlElement
    private VersionRange versionRange = null;
    
    private ComparisonResult comparisonResult = null;


    private ServerInfo(){ this.serverRootDir = null; } // for JAXB
    
    public ServerInfo( File serverRootDir ) {
        this.serverRootDir = serverRootDir;
    }
    
    
    /**
     *  Formats a string describing this server.
     */
    public String format() {
        return type.format( versionRange ) + " in " + this.serverRootDir;
    }

    public void compareHashes() throws MigrationException {
        if( ! ( this.type instanceof HasHashes ) )
            throw new MigrationException("Comparison of file hashes is not supported for server type '" + this.type.getDescription() + "'.");
            
        if( ! versionRange.isExactVersion() )
            log.warn("Comparing hashes without knowing exact server version. May produce a lot of mismatches.");
        
        this.comparisonResult = ((HasHashes)this.type).compareHashes( versionRange.from, serverRootDir );
    }

    

    public IServerType getType() { return type; }
    public ServerInfo setType( IServerType type ) { this.type = type; return this; }
    public VersionRange getVersionRange() { return versionRange; }
    public ServerInfo setVersionRange( VersionRange versionRange ) { this.versionRange = versionRange; return this; }

    public ComparisonResult getHashesComparisonResult() { return comparisonResult; }

    // JAXB
    @XmlAttribute @XmlWriteOnly
    private String getFormatted(){
        return this.format();
    }
    
}// class
