package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

import org.jboss.windup.graph.model.WindupVertexFrame;

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
@TypeValue("BlackListModel")
public interface BlackListModel extends WindupVertexFrame
{
    public static final String LABEL_FILE_MODEL = "fileModel";
    public static final String PROPERTY_HINT = "hint";
    public static final String PROPERTY_LINE_NUMBER = "lineNumber";
    public static final String PROPERTY_LENGTH = "length";
    public static final String PROPERTY_START_POSITION = "startPosition";
    public static final String PROPERTY_RULE_ID = "ruleID";
    public static final String PROPERTY_CLASS_CANDIDATE_TYPE = "candidateType";
    public static final String PROPERTY_EFFORT = "effort";

    /**
     * Sets the file model associated with this blacklist entry. Black List entries may be associated with any type of
     * file (Java, XML, etc)
     * 
     * @param fileModel
     */
    @Adjacency(label = LABEL_FILE_MODEL, direction = Direction.OUT)
    public void setFileModel(FileModel fileModel);

    @Adjacency(label = LABEL_FILE_MODEL, direction = Direction.OUT)
    public FileModel getFileModel();

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
     * Sets the effort needed to fix the issue
     * 
     * @param effort
     */
    @Property(PROPERTY_EFFORT)
    public void setEffort(int effort);

    @Property(PROPERTY_EFFORT)
    public int getEffort();
    
    /**
     * Sets the ID of the rule that triggered this particular blacklist entry
     * 
     * @param ruleID
     */
    @Property(PROPERTY_RULE_ID)
    public void setRuleID(String ruleID);

    @Property(PROPERTY_RULE_ID)
    public String getRuleID();

    /**
     * Sets the Line number position for this entry within the file
     * 
     * @return
     */
    @Property(PROPERTY_LINE_NUMBER)
    public void setLineNumber(int lineNumber);

    @Property(PROPERTY_LINE_NUMBER)
    public int getLineNumber();

    /**
     * Sets the start position for this entry within the line
     * 
     * @return
     */
    @Property(PROPERTY_START_POSITION)
    public void setStartPosition(int startPosition);

    @Property(PROPERTY_START_POSITION)
    public int getStartPosition();

    /**
     * Sets the length of this entry within the file
     * 
     * @param length
     */
    @Property(PROPERTY_LENGTH)
    public void setLength(int length);

    @Property(PROPERTY_LENGTH)
    public int getLength();

    /**
     * Sets the hint associated with this blacklist entry
     * 
     * @param hint
     */
    @Property(PROPERTY_HINT)
    public void setHint(String hint);

    @Property(PROPERTY_HINT)
    public String getHint();
}
