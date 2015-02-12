package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a technology that is used or implemented by a particular file. For example, this might indicate that a file uses "EJB" or that the file
 * is a Hibernate Configuration File.
 * 
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(TechnologyTagModel.TYPE)
public interface TechnologyTagModel extends WindupVertexFrame
{
    public static final String TYPE = "TechnologyTag";

    public static final String TECH_TAG_TO_FILE_MODEL = "techTagToFileModel";
    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String LEVEL = "level";

    /**
     * This should be a short tag representing the technology (eg, EJB)
     */
    @Property(NAME)
    String getName();

    /**
     * This should be a short tag representing the technology (eg, EJB)
     */
    @Property(NAME)
    void setName(String tag);

    /**
     * This should be a short tag representing the technology version (eg, 3.1)
     */
    @Property(VERSION)
    String getVersion();

    /**
     * This should be a short tag representing the technology version (eg, 3.1)
     */
    @Property(VERSION)
    void setVersion(String tag);

    /**
     * Contains information about the relative importance of this tag.
     */
    @Property(LEVEL)
    TechnologyTagLevel getLevel();

    /**
     * Contains information about the relative importance of this tag.
     */
    @Property(LEVEL)
    void setLevel(TechnologyTagLevel level);

    /**
     * References the {@link FileModel}s that use this technology.
     */
    @Adjacency(label = TECH_TAG_TO_FILE_MODEL, direction = Direction.OUT)
    public void addFileModel(FileModel fileModel);

    /**
     * References the {@link FileModel}s that use this technology.
     */
    @Adjacency(label = TECH_TAG_TO_FILE_MODEL, direction = Direction.OUT)
    public Iterable<FileModel> getFileModels();
}
