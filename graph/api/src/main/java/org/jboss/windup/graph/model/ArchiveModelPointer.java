package org.jboss.windup.graph.model;

import java.util.List;

/**
 *  Points to an ArchiveType contained in a module,
 *  so it can be loaded in ConfigureArchiveTypes.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public abstract class ArchiveModelPointer<T extends ArchiveModel> {

    /**
     * Which class represents the archive.
     */
    public abstract Class<T> getModelClass();
    
    /**
     * What's the suffix of the archive file of this type.
     */
    public abstract String getArchiveFileSuffix();

}// class
