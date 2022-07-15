package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.List;

/**
 * Represents a technology that is used or implemented by a particular file. For example, this might indicate that a file uses "EJB" or that the file
 * is a Hibernate Configuration File.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(TechnologyTagModel.TYPE)
public interface TechnologyTagModel extends WindupVertexFrame {
    String TYPE = "TechnologyTagModel";

    String TECH_TAG_TO_FILE_MODEL = "techTagToFileModel";
    String NAME = "name";
    String VERSION = "version";
    String LEVEL = "level";

    /**
     * This should be a short tag representing the technology (eg, EJB)
     */
    @Property(NAME)
    String getName();

    /**
     * This should be a short tag representing the technology (eg, EJB)
     */
    @Indexed
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
    void addFileModel(FileModel fileModel);

    /**
     * References the {@link FileModel}s that use this technology.
     */
    @Adjacency(label = TECH_TAG_TO_FILE_MODEL, direction = Direction.OUT)
    void removeFileModel(FileModel fileModel);

    /**
     * References the {@link FileModel}s that use this technology.
     */
    @Adjacency(label = TECH_TAG_TO_FILE_MODEL, direction = Direction.OUT)
    List<FileModel> getFileModels();
}
