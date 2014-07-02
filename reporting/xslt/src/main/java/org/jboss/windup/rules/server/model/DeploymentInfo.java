package org.jboss.windup.rules.server.model;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.DeploymentConfigUtils;

/**
 * Info about deployment provided by user as input.
 * The deployment may contain app-scoped configuration like -ds.xml, classloading etc.
 * This class only holds info for deployment extraction. Further analysis is up to IMigrators.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "deployment")
@XmlAccessorType( XmlAccessType.NONE )
public final class DeploymentInfo {
    
    // What user provided as parameter.
    @XmlAttribute(name = "path")
    private final String path;
    // The same as a canonical file. Used e.g. for map keys.
    private final File canonicalFile;
    
    // EAR, WAR, JAR?
    @XmlAttribute(name = "type")
    private final DeploymentConfigUtils.DeploymentType type;
    
    // Where did we extract to?
    private File unzippedToTmpDirectory = null;
    
    // Report dir where various reports go. Windup occupies index.html.
    @XmlAttribute(name = "reportDir")
    private File reportDir = null;
    
    

    /**
     *  Creates this from $deplPath. 
     *  Also, type is derived from name, and canonical file is derived for map keys.
     */
    public DeploymentInfo( String deplPath ) throws MigrationException {
        this( new File(deplPath) );
    }
    
    /**
     *  Creates this from $deplPath. 
     *  Also, type is derived from name, and canonical file is derived for map keys.
     */
    public DeploymentInfo( File deplPath ) throws MigrationException {
        try {
            this.canonicalFile = deplPath.getCanonicalFile();
        } catch( IOException | NullPointerException ex ) {
            throw new MigrationException( "Failed resolving canonical path for the deployment " + deplPath + ":\n    " + ex.getMessage(), ex );
        }
        this.path = deplPath.getPath();
        this.type = guessTypeFromName();
    }

    // For JAXB.
    DeploymentInfo() {
        path = null;
        canonicalFile = null;
        type = null;
    }
    
    
    
    private DeploymentConfigUtils.DeploymentType guessTypeFromName(){
        String suffix = StringUtils.substringAfterLast(path, ".");
        return DeploymentConfigUtils.DeploymentType.from( suffix );
    }
    

    public File unzipToTmpDir() throws MigrationException {
        this.unzippedToTmpDirectory = DeploymentConfigUtils.unzipDeployment( new File( path ) );
        return this.unzippedToTmpDirectory;
    }
    
    
    /**
     *  Return a directory with the extracted deployment, either original or unzipped.
     * @return 
     */
    public File getDirToScan(){
        if( this.unzippedToTmpDirectory != null )
            return this.unzippedToTmpDirectory;
        
        return new File( this.path );
    }
    
    public final File getAsCanonicalFile(){
        return canonicalFile;
    }
    
    
    /**
        EAR => myapp.ear/META-INF
        WAR => myapp.war/WEB-INF
        JAR => mylib.jar/META-INF
     */
    public File getInfDir(){
        return new File( getDirToScan(), this.type.getInfDir() );
    }


    // hashCode / equals delegated to path.
    @Override public boolean equals( Object anObject ) {
        return path.equals( anObject );
    }

    @Override public int hashCode() {
        return path.hashCode();
    }

    @Override public String toString() {
        return "DeploymentInfo{" + path + ", type " + type + ", report in " + reportDir + '}';
    }
    
    
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getUserProvidedPath() { return path; }
    public DeploymentConfigUtils.DeploymentType getType() { return type; }
    public File getUnzippedToTmpDirectory() { return unzippedToTmpDirectory; }
    public File getReportDir() { return reportDir; }
    public DeploymentInfo setReportDir( File reportDir ) { this.reportDir = reportDir; return this; }
    //</editor-fold>

}// class
