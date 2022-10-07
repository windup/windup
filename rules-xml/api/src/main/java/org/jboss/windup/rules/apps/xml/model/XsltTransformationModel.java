package org.jboss.windup.rules.apps.xml.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.xml.operation.xslt.XSLTTransformation;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

/**
 * Contains metadata regarding the result of an XSLT Transformation
 */
@TypeValue(XsltTransformationModel.TYPE)
public interface XsltTransformationModel extends WindupVertexFrame {
    public static final String TYPE = "XsltTransformationModel";
    public static final String LOCATION = TYPE + "-location";
    public static final String EXTENSION = TYPE + "-extension";
    public static final String EFFORT = TYPE + "-effort";
    public static final String DESCRIPTION = TYPE + "-description";
    public static final String FILE_SOURCE = TYPE + "-file_source";
    public static final String FILE_RESULT = TYPE + "-file_result";

    /**
     * Contains the location of the XSLT file
     */
    @Property(LOCATION)
    public String getSourceLocation();

    /**
     * Contains the location of the XSLT file
     */
    @Property(LOCATION)
    public void setSourceLocation(String location);

    /**
     * Contains the suffix of the result file that is going to be added.
     */
    @Property(EXTENSION)
    public String getExtension();

    /**
     * Contains the suffix of the result file
     */
    @Property(EXTENSION)
    public void setExtension(String extension);

    /**
     * Contains descriptive text describing the result
     */
    @Property(DESCRIPTION)
    public String getDescription();

    /**
     * Contains descriptive text describing the result
     */
    @Property(DESCRIPTION)
    public void setDescription(String description);

    /**
     * Gets the relative level of effort to complete this {@link XSLTTransformation}
     */
    @Property(EFFORT)
    public int getEffort();

    /**
     * Sets the relative level of effort to complete this {@link XSLTTransformation}
     */
    @Property(EFFORT)
    public void setEffort(int effort);

    /**
     * Links to the original {@link FileModel} with the original XML contents.
     */
    @Adjacency(label = FILE_SOURCE, direction = Direction.OUT)
    FileModel getSourceFile();

    /**
     * Links to the original {@link FileModel} with the original XML contents.
     */
    @Adjacency(label = FILE_SOURCE, direction = Direction.OUT)
    void setSourceFile(FileModel file);

    /**
     * Contains the result filename
     */
    @Property(FILE_RESULT)
    String getResult();

    /**
     * Contains the result filename
     */
    @Property(FILE_RESULT)
    void setResult(String path);
}
