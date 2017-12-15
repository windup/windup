package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains information regarding a JBPM 3 process model.
 */
@TypeValue(Jbpm3ProcessModel.TYPE)
public interface Jbpm3ProcessModel extends XmlFileModel
{
    String TYPE = "Jbpm3ProcessModel";
    String PROCESS_NAME = "processName";
    String STATE_COUNT = "stateCount";
    String NODE_COUNT = "nodeCount";
    String DECISION_COUNT = "decisionCount";
    String FORK_COUNT = "forkCount";
    String SUBPROCESS_COUNT = "subProcessCount";
    String TASK_COUNT = "taskCount";
    String ACTION_HANDLERS = "actionHandlers";
    String DECISION_HANDLERS = "decisionHandlers";

    /**
     * Contains the name of the process.
     */
    @Property(PROCESS_NAME)
    String getProcessName();

    /**
     * Contains the name of the process.
     */
    @Property(PROCESS_NAME)
    String setProcessName(String processName);

    /**
     * Contains a link to an image representing the process.
     */
    @Adjacency(label = ReportResourceFileModel.TYPE, direction = Direction.OUT)
    ReportResourceFileModel getProcessImage();

    /**
     * Contains a link to an image representing the process.
     */
    @Adjacency(label = ReportResourceFileModel.TYPE, direction = Direction.OUT)
    void setProcessImage(ReportResourceFileModel processImage);
    
    @Property(DECISION_COUNT)
    Integer getDecisionCount();

    @Property(DECISION_COUNT)
    Integer setDecisionCount(Integer decisionCount);
    
    @Property(STATE_COUNT)
    Integer getStateCount();

    @Property(STATE_COUNT)
    Integer setStateCount(Integer stateCount);
    
    @Property(NODE_COUNT)
    Integer getNodeCount();

    @Property(NODE_COUNT)
    Integer setNodeCount(Integer nodeCount);
    
    @Property(FORK_COUNT)
    Integer getForkCount();

    @Property(FORK_COUNT)
    Integer setForkCount(Integer forkCount);
    
    @Property(SUBPROCESS_COUNT)
    Integer getSubProcessCount();

    @Property(SUBPROCESS_COUNT)
    Integer setSubProcessCount(Integer subProcessCount);

    @Property(TASK_COUNT)
    Integer getTaskCount();

    @Property(TASK_COUNT)
    Integer setTaskCount(Integer taskCount);

    /**
     * Contains a list of action handlers used by this process.
     */
    @Adjacency(label = ACTION_HANDLERS, direction = Direction.OUT)
    void addActionHandler(final JavaClassModel javaClass);

    /**
     * Contains a list of action handlers used by this process.
     */
    @Adjacency(label = ACTION_HANDLERS, direction = Direction.OUT)
    Iterable<JavaClassModel> getActionHandlers();

    /**
     * Contains a list of decision handlers used by this process.
     */
    @Adjacency(label = DECISION_HANDLERS, direction = Direction.OUT)
    void addDecisionHandler(final JavaClassModel javaClass);

    /**
     * Contains a list of decision handlers used by this process.
     */
    @Adjacency(label = DECISION_HANDLERS, direction = Direction.OUT)
    Iterable<JavaClassModel> getDecisionHandlers();

}
