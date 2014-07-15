package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;

import com.tinkerpop.frames.Property;

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
