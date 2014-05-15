package org.jboss.loom.recog;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IProjectAndProductVersionBidiMapper {
    
    public String getProjectToProductVersion( String str );
    
    public String getProductToProjectVersion( String str );
    
}
