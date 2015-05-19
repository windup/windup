package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;


@TypeValue(Jbpm3ProcessModel.TYPE)
public interface Jbpm3ProcessModel extends XmlFileModel
{
    public static final String TYPE = "Jbpm3ProcessModel";
    public static final String PROCESS_NAME = "processName";
    public static final String STATE_COUNT = "stateCount";
    public static final String NODE_COUNT = "nodeCount";
    public static final String DECISION_COUNT = "decisionCount";
    public static final String FORK_COUNT = "forkCount";
    public static final String SUBPROCESS_COUNT = "subProcessCount";
    public static final String TASK_COUNT = "taskCount";

    @Property(PROCESS_NAME)
    public String getProcessName();

    @Property(PROCESS_NAME)
    public String setProcessName(String processName);
    
    @Adjacency(label = ReportResourceFileModel.TYPE, direction = Direction.OUT)
    public ReportResourceFileModel getProcessImage();
    
    @Adjacency(label = ReportResourceFileModel.TYPE, direction = Direction.OUT)
    public void setProcessImage(ReportResourceFileModel processImage);
    
    @Property(DECISION_COUNT)
    public Integer getDecisionCount();

    @Property(DECISION_COUNT)
    public Integer setDecisionCount(Integer decisionCount);
    
    @Property(STATE_COUNT)
    public Integer getStateCount();

    @Property(STATE_COUNT)
    public Integer setStateCount(Integer stateCount);
    
    @Property(NODE_COUNT)
    public Integer getNodeCount();

    @Property(NODE_COUNT)
    public Integer setNodeCount(Integer nodeCount);
    
    @Property(FORK_COUNT)
    public Integer getForkCount();

    @Property(FORK_COUNT)
    public Integer setForkCount(Integer forkCount);
    
    @Property(SUBPROCESS_COUNT)
    public Integer getSubProcessCount();

    @Property(SUBPROCESS_COUNT)
    public Integer setSubProcessCount(Integer subProcessCount);

    @Property(TASK_COUNT)
    public Integer getTaskCount();

    @Property(TASK_COUNT)
    public Integer setTaskCount(Integer taskCount);
    
    @Adjacency(label = "actionHandlers", direction = Direction.OUT)
    void addActionHandler(final JavaClassModel javaClass);

    @Adjacency(label = "actionHandlers", direction = Direction.OUT)
    Iterable<JavaClassModel> getActionHandlers();

    
    @Adjacency(label = "decisionHandlers", direction = Direction.OUT)
    void addDecisionHandler(final JavaClassModel javaClass);

    @Adjacency(label = "decisionHandlers", direction = Direction.OUT)
    Iterable<JavaClassModel> getDecisionHandlers();

}
