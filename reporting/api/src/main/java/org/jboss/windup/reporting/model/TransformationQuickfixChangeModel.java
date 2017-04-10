package org.jboss.windup.reporting.model;

import static org.jboss.windup.reporting.model.TransformationQuickfixChangeModel.TYPE_VALUE;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(TYPE_VALUE)
public interface TransformationQuickfixChangeModel extends WindupVertexFrame
{
	String TYPE_VALUE = "TransformationQuickfixChange";
    String LOCATION = TYPE_VALUE + "-location";
    String APPLIED = TYPE_VALUE + "-applied";
    String FILE_MODEL = TYPE_VALUE + "-file";
    String TITLE = TYPE_VALUE + "-title";
    String DESCRIPTION = TYPE_VALUE + "-description";
    
    @Property(TITLE)
    void setTitle(String title);

    @Property(TITLE)
    String getTitle();

    @Property(DESCRIPTION)
    void setDescription(String description);

    @Property(DESCRIPTION)
    String getDescription();
    
    @Property(APPLIED)
    boolean isApplied();
    
    @Property(APPLIED)
    void setApplied(boolean applied);
	
    @Adjacency(label = LOCATION, direction = Direction.OUT)
    void setLocation(LocationDataModel locationModel);

    @Adjacency(label = LOCATION, direction = Direction.OUT)
    LocationDataModel getLocation();
    
    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    FileModel getFile();

    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    void setFile(FileModel file);

    /**
     * Preview this change.
     */
	String preview();
	/**
	 * Apply this change.
	 */
	void apply();
}
