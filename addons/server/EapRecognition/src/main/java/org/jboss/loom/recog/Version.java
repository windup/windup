package org.jboss.loom.recog;

import java.util.Objects;

/**
 *  Some versions represent products.
 *  Products have own versioning scheme, but are mappable to project versions.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Version {

    public String verProject;
    public String verProduct;

    public Version( String version ) {
        this.verProject = version;
    }
    
    public Version( String projectVer, String productVer ) {
        this.verProject = projectVer;
        this.verProduct = productVer;
    }
    
    /**
     *  Auto-fills the product version by looking it up through given mapper.
     */
    public Version( String version, IProjectAndProductVersionBidiMapper mapper ) {
        this.verProject = version;
        this.verProduct = mapper.getProjectToProductVersion( version );
    }
    
    /**
     *  Compares using project version.
     */
    public int compare( Version other ) {
        return VersionComparer.compareVersions( this.verProject, other.verProject );
    }


    @Override
    public String toString() {
        if( verProduct == null )  return verProject;
        return verProject + '(' + verProduct + ')';
    }
    
    public Object toString_preferProduct() {
        return this.verProduct != null ? this.verProduct : this.verProject;
    }
    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode( this.verProject );
        hash = 61 * hash + Objects.hashCode( this.verProduct );
        return hash;
    }


    @Override
    public boolean equals( Object obj ) {
        if( obj == null )  return false;
        if( getClass() != obj.getClass() )  return false;
        
        final Version other = (Version) obj;
        if( ! Objects.equals( this.verProject, other.verProject ) )  return false;
        if( ! Objects.equals( this.verProduct, other.verProduct ) )  return false;
        
        return true;
    }
    
    
    private Version() { } // JAXB

}// class
