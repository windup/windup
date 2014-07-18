package org.jboss.windup.rules.apps.legacy.java;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.blacklist.BlackListRegex;
import org.jboss.windup.rules.apps.java.blacklist.ModelCreatorGraphOperation;
import org.jboss.windup.rules.apps.java.blacklist.Types;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class JBossJBPMConfig extends WindupRuleProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
     
        List<BlackListRegex> hints = new ArrayList<BlackListRegex>();
        hints.add(new BlackListRegex(getID(), "org.jbpm.graph.def.ActionHandler$", "Migrate to jBPM 5 org.drools.runtime.process.WorkItemHandler.", 2, Types.add(ClassCandidateType.INHERITANCE)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.db.TaskMgmtSession$", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession", 4));
        hints.add(new BlackListRegex(getID(), "org.jbpm.JbpmContext.getTaskInstance\\(.+\\)", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTask(long taskId)", 0));
        hints.add(new BlackListRegex(getID(), "org.jbpm.db.TaskMgmtSession.getTaskInstance\\( ", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTask(long taskId)", 0));
        hints.add(new BlackListRegex(getID(), "org.jbpm.db.TaskMgmtSession.loadTaskInstance\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTask(long taskId)", 0));
        hints.add(new BlackListRegex(getID(), "org.jbpm.db.TaskMgmtSession.findTaskInstances\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTasksOwned(String userId, String language)", 0));
        hints.add(new BlackListRegex(getID(), "org.jbpm.db.TaskMgmtSession.findTaskInstances\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTasksOwned(String userId, List<Status> status, String language)", 0));
        hints.add(new BlackListRegex(getID(), "org.jbpm.db.TaskMgmtSession.findTaskInstancesByProcessInstance\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTasksByStatusByProcessId(long processInstanceId, List<Status> status, String language)", 0));
        hints.add(new BlackListRegex(getID(), "org.jbpm.db.TaskMgmtSession.findTaskInstancesByProcessInstance\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTasksByStatusByProcessIdByTaskName(long processInstanceId, List<Status> status, String taskName, String language)", 0));
        hints.add(new BlackListRegex(getID(), "org.jbpm.db.TaskMgmtSession.findTaskInstancesByToken\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTaskByWorkItemId(long workItemId)", 0));
        hints.add(new BlackListRegex(getID(), "org.jbpm.JbpmConfiguration$", "Migrate to jBPM 5: Replace with creation of org.drools.KnowledgeBase.", 1));
        hints.add(new BlackListRegex(getID(), "org.jbpm.JbpmConfiguration.createJbpmContext\\(\\)", "Migrate to jBPM 5: Replace with instantiation of org.drools.runtime.StatefulKnowledgeSession.", 1));
        hints.add(new BlackListRegex(getID(), "org.jbpm.JbpmContext.newProcessInstance", "Migrate to jBPM 5: org.drools.runtime.StatefulKnowledgeSession.startProcess(String processId)", 1));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.getPriority\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getPriority or org.jbpm.task.Task.getPriority", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.getName\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getName or org.jbpm.task.Task.getNames", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.getId\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getId or org.jbpm.task.Task.getId", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.getCreate\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getCreatedOn", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.getDescription\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getDescription or org.jbpm.task.Task.getDescriptions", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.getStart\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getActivationTime", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.end\\(", "<![CDATA["
                    + "Migrate to jBPM 5:\n" + 
                    "            org.jbpm.task.service.TaskClient\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            TaskClient client = new TaskClient(new MinaTaskClientConnector(...);\n" + 
                    "            client.complete( taskId, ...);\n" + 
                    "            ```"
                    + "]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.addComment\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.addComment", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.getComments\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.addComment", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.getProcessInstance\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getProcessInstanceId", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.getActorId\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getActualOwner", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.getDueDate\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getExpirationTime", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.isBlocking\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.isSkipable", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.getDueDate\\(", "Migrate to jBPM 5: org.jbpm.task.Task.getDeadlines", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.start\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Activate", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.setActorId\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Claim", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.resume\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Resume", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.cancel\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Skip", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.start\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Start", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.exe.TaskInstance.suspend\\(", "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Suspend", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.def.Task.getPriority\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getPriority or org.jbpm.task.Task.getPriority", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.def.Task.getName\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getName or org.jbpm.task.Task.getNames", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.def.Task.getId\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getId or org.jbpm.task.Task.getId", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.def.Task.getDescription\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getDescription or org.jbpm.task.Task.getDescriptions", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.def.Task.isBlocking\\(", "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.isSkipable", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.def.Task.getAssignmentDelegation\\(", "Migrate to jBPM 5: org.jbpm.task.Task.getDelegation", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.taskmgmt.def.Task.getDueDate\\(", "Migrate to jBPM 5: org.jbpm.task.Task.getDeadlines", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.graph.exe.ProcessInstance$", "Migrate to jBPM 5: org.drools.runtime.process.ProcessInstance", 0));
        hints.add(new BlackListRegex(getID(), "org.jbpm.graph.exe.ExecutionContext.getVariable", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            kcontext.setProcessInstance(processInstance);\n" + 
                    "            kcontext.getVariable(...);\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.graph.exe.ExecutionContext.setVariable", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            kcontext.setProcessInstance(processInstance);\n" + 
                    "            kcontext.setVariable(...);\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.JbpmContext.getProcessInstance", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.ContextInstance.getVariables", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            kcontext.setProcessInstance(processInstance);\n" + 
                    "            kcontext.getVariable(...);\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.ContextInstance.setVariable", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            kcontext.setProcessInstance(processInstance);\n" + 
                    "            kcontext.setVariable(...);\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.ContextInstance.getVariable", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            kcontext.setProcessInstance(processInstance);\n" + 
                    "            kcontext.getVariable(...);\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.VariableContainer.setVariable", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            kcontext.setProcessInstance(processInstance);\n" + 
                    "            kcontext.setVariable(...);\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.VariableContainer.getVariable", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            kcontext.setProcessInstance(processInstance);\n" + 
                    "            kcontext.getVariable(...);\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.VariableContainer.getContextInstance ", "Migrate to jBPM 5: org.jbpm.process.instance.context.variable.VariableScopeInstance.getVariableScope", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.VariableContainer.setVariables", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            kcontext.setProcessInstance(processInstance);\n" + 
                    "            kcontext.setVariable(...);\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.VariableInstance.getName", "Migrate to jBPM 5: org.jbpm.process.core.context.variable.Variable.getName", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.VariableInstance.getValue", "Migrate to jBPM 5: org.jbpm.process.core.context.variable.Variable.getValue", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.VariableInstance.setValue", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            kcontext.setProcessInstance(processInstance);\n" + 
                    "            kcontext.setVariable(...);\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.VariableInstance.toString", "Migrate to jBPM 5: org.jbpm.process.core.context.variable.Variable.toString", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.ContextInstance.getVariable", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            kcontext.setProcessInstance(processInstance);\n" + 
                    "            kcontext.getVariable(...);\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "org.jbpm.context.exe.ContextInstance.setVariable", "<![CDATA[\n" + 
                    "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                    "\n" + 
                    "            ```java\n" + 
                    "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                    "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                    "            kcontext.setProcessInstance(processInstance);\n" + 
                    "            kcontext.setVariable(...);\n" + 
                    "            ```\n" + 
                    "        ]]>", 0, Types.add(ClassCandidateType.METHOD))); 
        
        
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule().perform(new ModelCreatorGraphOperation().add(hints));
        return configuration;
        
    }
}
