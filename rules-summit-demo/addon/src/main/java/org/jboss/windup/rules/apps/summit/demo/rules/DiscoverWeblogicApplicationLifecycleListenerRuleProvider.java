package org.jboss.windup.rules.apps.summit.demo.rules;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.tooling.TransformationHintService;
import org.jboss.windup.tooling.data.HintImpl;
import org.jboss.windup.tooling.data.IssueCategoryImpl;
import org.jboss.windup.tooling.data.Quickfix;
import org.jboss.windup.tooling.data.TransformationQuickfix;
import org.jboss.windup.tooling.data.TransformationQuickfixChange;
import org.jboss.windup.tooling.data.TransformationQuickfixChangeImpl;
import org.jboss.windup.tooling.data.TransformationQuickfixImpl;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.google.common.collect.Lists;

/**
 * Discovers WebLogic application lifecycle listeners registered within the weblogic-application.xml file.
 */
@RuleMetadata(phase = InitialAnalysisPhase.class)
public class DiscoverWeblogicApplicationLifecycleListenerRuleProvider extends AbstractRuleProvider
{
    private static Logger LOG = Logger.getLogger(DiscoverWeblogicApplicationLifecycleListenerRuleProvider.class.getName());
    
    private static final String XML_LISTENER_RULE_ID = "weblogic-application-lifecycle-rule-id";
    private static final String XML_TITLE = "Weblogic application lifecycle listener.";
    private static final String XML_HINT = "Application lifecycle listeners registered within weblogic-appplication.xml need to be refactored.";
	
    @Inject
    private TransformationHintService transformationQuickfixService;
 
	@Override
	public Configuration getConfiguration(RuleLoaderContext context) 
	{
		return ConfigurationBuilder.begin()
				.addRule()
				.when(Query.fromType(XmlFileModel.class).withProperty(FileModel.FILE_NAME, "weblogic-application.xml")
						.withProperty(XmlFileModel.ROOT_TAG_NAME, "weblogic-application"))
				.perform(new AbstractIterationOperation<XmlFileModel>() {
					 public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel fileModel) {
						 Hint.withText(XML_HINT).skipTooling().withEffort(8);
						 createToolingHint(fileModel, event);
					 }
				}).withId(XML_LISTENER_RULE_ID);
	}
	
	private void createToolingHint(XmlFileModel fileModel, GraphRewrite event)
	{
		HintImpl hint = new HintImpl(XML_LISTENER_RULE_ID);
		hint.setFile(fileModel.asFile());
        hint.setTitle(XML_TITLE);
        hint.setHint(XML_HINT);
        IssueCategoryModel categoryModel = IssueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.MANDATORY);
        hint.setIssueCategory(new IssueCategoryImpl(categoryModel.getCategoryID(), categoryModel.getOrigin(),
        		categoryModel.getName(), categoryModel.getDescription(), categoryModel.getPriority()));
        hint.setEffort(8);
        hint.setColumn(1);
        hint.setLineNumber(1);
        hint.setLength(2);
        hint.setRuleID(XML_LISTENER_RULE_ID);
        
		transformationQuickfixService.addHint(hint);
		
		List<Quickfix> quickfixes = Lists.newArrayList();
		TransformationQuickfix quickfix = new TransformationQuickfixImpl();
		quickfixes.add(quickfix);
		
		createChanges(quickfix, fileModel, event);
		
		hint.setQuickfixes(quickfixes);
	}
	
	private void createChanges(TransformationQuickfix quickfix, XmlFileModel fileModel, GraphRewrite event) 
	{
		TransformationQuickfixChange change = new TransformationQuickfixChangeImpl() {
			@Override
			public String preview() {
				return "Preview...";
			}
			public void apply() {
				System.out.println("Applying...");
			}
		};
		change.setFile(fileModel.asFile());
		change.setDescription("Weblogic proprietary application lifecycle listener detected.");
		change.setDescription("Remove the <listener>...</listener> tag");
		ChangeConfig config = computeXmlChange(fileModel);
		change.setSnippet(config.snippet);
		change.setStartPosition(config.startPosition);
		change.setLineNumber(config.lineNumber);
		change.setLength(config.length);
		quickfix.addChange(change);
	}
	
	private ChangeConfig computeXmlChange(XmlFileModel fileModel)
	{
		ChangeConfig config = new ChangeConfig();
		
		return config;
	}
	
	private String findXMLListenerSnippet(XmlFileModel model)
	{
		return "";
	}
	
	private static class ChangeConfig {
		private String snippet;
		private int lineNumber;
		private int startPosition;
		private int length;
	}
}
