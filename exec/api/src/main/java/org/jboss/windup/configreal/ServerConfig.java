package org.jboss.windup.configreal;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ServerConfig {
    
    Path path;


    public Path getPath()
    {
        return path;
    }


    public void setPath(Path path)
    {
        this.path = path;
    }
    
    public void setPath(String path)
    {
        this.path = Paths.get(path);
    }
    
    

}// class
