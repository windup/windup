package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;

import com.tinkerpop.frames.Property;

/**
 * Class used to define multiple {@link ClassCandidateType}'s for one vertex (since List<CandidateType> cannot be put into properties)
 * 
 */
public interface ClassCandidateTypeModel extends WindupVertexFrame
{

    public static final String PROPERTY_CLASS_CANDIDATE_TYPE = "classCandidateType";
    
    /**
     * Sets the candidateType which is represented by this vertex
     * 
     * @param classCandidateType
     */
    @Property(PROPERTY_CLASS_CANDIDATE_TYPE)
    public void setClassCandidateType(ClassCandidateType classCandidateType);

    @Property(PROPERTY_CLASS_CANDIDATE_TYPE)
    public ClassCandidateType getClassCandidateType();
}
