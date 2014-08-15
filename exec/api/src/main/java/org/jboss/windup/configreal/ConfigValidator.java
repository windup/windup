package org.jboss.windup.configreal;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ConfigValidator {
    private static final Logger log = LoggerFactory.getLogger(ConfigValidator.class);
    

    /**
     *  Validates the config - checks if the paths exist, contain the expected files etc.
     * 
     *  @returns  True if everything is OK.
     */
    public static List<String> validate(WindupConfig config) {
        LinkedList<String> problems = new LinkedList<>();
        
        {
            // Source server
            Path path = config.getCoreConfig().getSrcServerConfig().getPath();
            if( null == path )
                problems.add("srcServer.dir was not set.");
            else if( ! path.toFile().isDirectory() )
                problems.add("srcServer.dir is not a directory: " + path);

            // Dest Server
            path = config.getCoreConfig().getDestServerConfig().getPath();
            if( null == path )
                problems.add("destServer.dir was not set.");
            else if( ! path.toFile().isDirectory() )
                problems.add("destServer.dir is not a directory: " + path);
        }

        
        // App (deployment)
        Set<Path> paths = config.getCoreConfig().getDeploymentsPaths();
        for( Path path : paths ) {
            if( null != path && ! path.toFile().exists())
                problems.add("App path was set but does not exist: " + path);
        }
        
        return problems;
    }

}// class
