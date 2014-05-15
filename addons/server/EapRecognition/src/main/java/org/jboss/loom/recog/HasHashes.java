package org.jboss.loom.recog;

import java.io.File;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.utils.compar.ComparisonResult;

/**
 * Marks IServerType which is capable of comparing given dir tree against a list of given version's dist files hashes.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface HasHashes {
    

    /**
     *  Compares hash files of distribution of given $version against files in $serverRootDir
     */
    public ComparisonResult compareHashes( Version version, File serverRootDir ) throws MigrationException;

}
