package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
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

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.graph.def.ActionHandler$") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5 org.drools.runtime.process.WorkItemHandler." ).withEffort( 2 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.db.TaskMgmtSession$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession" ).withEffort( 4 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.JbpmContext.getTaskInstance\\(.+\\)") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTask(long taskId)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.db.TaskMgmtSession.getTaskInstance\\( ") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTask(long taskId)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.db.TaskMgmtSession.loadTaskInstance\\(") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTask(long taskId)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.db.TaskMgmtSession.findTaskInstances\\(") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTasksOwned(String userId, String language)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.db.TaskMgmtSession.findTaskInstances\\(") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTasksOwned(String userId, List<Status> status, String language)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.db.TaskMgmtSession.findTaskInstancesByProcessInstance\\(") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTasksByStatusByProcessId(long processInstanceId, List<Status> status, String language)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.db.TaskMgmtSession.findTaskInstancesByProcessInstance\\(") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTasksByStatusByProcessIdByTaskName(long processInstanceId, List<Status> status, String taskName, String language)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.db.TaskMgmtSession.findTaskInstancesByToken\\(") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.getTaskByWorkItemId(long workItemId)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.JbpmConfiguration$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: Replace with creation of org.drools.KnowledgeBase." ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.JbpmConfiguration.createJbpmContext\\(\\)") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: Replace with instantiation of org.drools.runtime.StatefulKnowledgeSession." ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.JbpmContext.newProcessInstance") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.drools.runtime.StatefulKnowledgeSession.startProcess(String processId)" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.getPriority\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getPriority or org.jbpm.task.Task.getPriority" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.getName\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getName or org.jbpm.task.Task.getNames" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.getId\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getId or org.jbpm.task.Task.getId" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.getCreate\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getCreatedOn" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.getDescription\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getDescription or org.jbpm.task.Task.getDescriptions" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.getStart\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getActivationTime" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.end\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( null ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.addComment\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.addComment" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.getComments\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.addComment" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.getProcessInstance\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getProcessInstanceId" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.getActorId\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getActualOwner" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.getDueDate\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getExpirationTime" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.isBlocking\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.isSkipable" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.getDueDate\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.Task.getDeadlines" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.start\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Activate" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.setActorId\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Claim" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.resume\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Resume" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.cancel\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Skip" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.start\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Start" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.exe.TaskInstance.suspend\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.service.TaskServiceSession.taskOperation(...) with the parameter: org.jbpm.task.service.Operation.Suspend" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.def.Task.getPriority\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getPriority or org.jbpm.task.Task.getPriority" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.def.Task.getName\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getName or org.jbpm.task.Task.getNames" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.def.Task.getId\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getId or org.jbpm.task.Task.getId" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.def.Task.getDescription\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.getDescription or org.jbpm.task.Task.getDescriptions" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.def.Task.isBlocking\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.query.TaskSummary.isSkipable" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.def.Task.getAssignmentDelegation\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.Task.getDelegation" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.taskmgmt.def.Task.getDueDate\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.task.Task.getDeadlines" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.graph.exe.ProcessInstance$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.drools.runtime.process.ProcessInstance" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.graph.exe.ExecutionContext.getVariable") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            kcontext.setProcessInstance(processInstance);\n" + 
                                "            kcontext.getVariable(...);\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.graph.exe.ExecutionContext.setVariable") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            kcontext.setProcessInstance(processInstance);\n" + 
                                "            kcontext.setVariable(...);\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.JbpmContext.getProcessInstance") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.ContextInstance.getVariables") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            kcontext.setProcessInstance(processInstance);\n" + 
                                "            kcontext.getVariable(...);\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.ContextInstance.setVariable") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            kcontext.setProcessInstance(processInstance);\n" + 
                                "            kcontext.setVariable(...);\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.ContextInstance.getVariable") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            kcontext.setProcessInstance(processInstance);\n" + 
                                "            kcontext.getVariable(...);\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.VariableContainer.setVariable") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            kcontext.setProcessInstance(processInstance);\n" + 
                                "            kcontext.setVariable(...);\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.VariableContainer.getVariable") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            kcontext.setProcessInstance(processInstance);\n" + 
                                "            kcontext.getVariable(...);\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.VariableContainer.getContextInstance ") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.process.instance.context.variable.VariableScopeInstance.getVariableScope" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.VariableContainer.setVariables") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            kcontext.setProcessInstance(processInstance);\n" + 
                                "            kcontext.setVariable(...);\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.VariableInstance.getName") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.process.core.context.variable.Variable.getName" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.VariableInstance.getValue") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.process.core.context.variable.Variable.getValue" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.VariableInstance.setValue") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            kcontext.setProcessInstance(processInstance);\n" + 
                                "            kcontext.setVariable(...);\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.VariableInstance.toString") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to jBPM 5: org.jbpm.process.core.context.variable.Variable.toString" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.ContextInstance.getVariable") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            kcontext.setProcessInstance(processInstance);\n" + 
                                "            kcontext.getVariable(...);\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.jbpm.context.exe.ContextInstance.setVariable") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Migrate to jBPM 5: org.drools.runtime.process.ProcessContext\n" + 
                                "\n" + 
                                "            ```java\n" + 
                                "            ProcessContext kcontext = new ProcessContext(ksession);\n" + 
                                "            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());\n" + 
                                "            kcontext.setProcessInstance(processInstance);\n" + 
                                "            kcontext.setVariable(...);\n" + 
                                "            ```\n" + 
                                "        ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    );

        return configuration;
    }
    // @formatter:on
}
