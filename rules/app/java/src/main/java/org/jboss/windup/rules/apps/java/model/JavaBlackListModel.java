package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.reporting.model.BlackListModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This indicates that a particular segment of code references a "black listed" API. This will indicate the
 * startPosition and length of the reference within the file for highlighting in a report or viewer.
 * 
 * Also, reference to the rule that originated the black list, as well as to instructions for correcting the issue will
 * be provided.
 * 
 * @author jsightler
 * 
 */
@TypeValue("JavaBlackListModel")
public interface JavaBlackListModel extends BlackListModel
{

    public static final String PROPERTY_QUALIFIED_NAME = "qualifiedName";
    public static final String PROPERTY_CLASS_CANDIDATE_TYPE = "candidateType";

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

}
