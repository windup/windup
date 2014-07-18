package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.ClassificationModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JavaClassificationModel")
public interface JavaClassificationModel extends ClassificationModel
{
    public static final String PROPERTY_QUALIFIED_NAME = "qualifiedName";
    public static final String PROPERTY_CLASS_CANDIDATE_TYPE = "classCandidateType";

    /**
     * Adds a class candidate type (e.g. IMPORT, TYPE etc.)
     * 
     * @param type
     */
    @Adjacency(label = PROPERTY_CLASS_CANDIDATE_TYPE, direction = Direction.OUT)
    public void addClassCandidateType(ClassCandidateTypeModel type);

    @Adjacency(label = PROPERTY_CLASS_CANDIDATE_TYPE, direction = Direction.OUT)
    public Iterable<ClassCandidateTypeModel> getClassCandidateType();

    /**
     * Sets the Qualified name of the entity being referenced (fully qualified classname in the case of a Java blacklist
     * entry)
     * 
     * @param qualifiedName
     */
    @Property(PROPERTY_QUALIFIED_NAME)
    public void setQualifiedName(String qualifiedName);

    @Property(PROPERTY_QUALIFIED_NAME)
    public String getQualifiedName();

    /**
     * Sets the JavaClassModel referenced by this entry. This could be null if none were found.
     * 
     * @param javaClassModel
     */
    @Adjacency(label = "referencedJavaClassModel", direction = Direction.OUT)
    public void setReferencedJavaClassModel(JavaClassModel javaClassModel);

    @Adjacency(label = "referencedJavaClassModel", direction = Direction.OUT)
    public JavaClassModel getReferencedJavaClassModel();

}
