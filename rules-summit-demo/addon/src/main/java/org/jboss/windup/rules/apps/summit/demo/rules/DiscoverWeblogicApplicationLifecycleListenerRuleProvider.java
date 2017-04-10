package org.jboss.windup.rules.apps.summit.demo.rules;

import static org.joox.JOOX.$;

import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.text.IRegion;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.QuickfixModel;
import org.jboss.windup.reporting.model.QuickfixType;
import org.jboss.windup.reporting.quickfix.Quickfix;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.util.xml.LocationAwareContentHandler;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers WebLogic application lifecycle listeners registered within the weblogic-application.xml file.
 */
@RuleMetadata(after = MigrationRulesPhase.class)
public class DiscoverWeblogicApplicationLifecycleListenerRuleProvider extends AbstractRuleProvider
{
    private static Logger LOG = Logger.getLogger(DiscoverWeblogicApplicationLifecycleListenerRuleProvider.class.getName());
    
    private static final String XML_LISTENER_RULE_ID = "weblogic-application-lifecycle-rule-id";
    private static final String XML_TITLE = "Weblogic application lifecycle listener.";
    private static final String XML_HINT = "Application lifecycle listeners registered within weblogic-appplication.xml need to be refactored.";
    private static final String XML_QUICKFIX_NAME = "Remove the <listener>...</listener> tag.";
    private static final String JAVA_QUICKFIX_NAME = "Refactor ApplicationLifecycleListener to use @ApplicationScoped.";
	
	@Override
	public Configuration getConfiguration(RuleLoaderContext context) 
	{
		return ConfigurationBuilder.begin()
				.addRule()
				.when(Query.fromType(XmlFileModel.class).withProperty(FileModel.FILE_NAME, "weblogic-application.xml")
						.withProperty(XmlFileModel.ROOT_TAG_NAME, "weblogic-application"))
				.perform(new AbstractIterationOperation<XmlFileModel>() {
					 public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel fileModel) {
						 try 
						 {
							 createHints(event, context, fileModel);
						 }
						 catch (Exception e)
						 {
							 LOG.severe(e.getMessage());
						 }
					 }
				}).withId(XML_LISTENER_RULE_ID);
	}
	
	private void createHints(GraphRewrite event, EvaluationContext context, XmlFileModel fileModel) throws Exception
	{
		GraphService<InlineHintModel> hintService = new GraphService<>(event.getGraphContext(), InlineHintModel.class);
		
		Document document = fileModel.asDocument();
		List<Element> listeners = $(document).find("listener-class").get();
		for (Element listener : listeners) 
		{
			String listenerClassName = listener.getTextContent();
			if(StringUtils.isNotBlank(listenerClassName))
			{
				InlineHintModel hintModel = createHint(event, hintService, fileModel);
				hintModel.addQuickfix(createXmlQuickfix(event.getGraphContext(), fileModel));
				int lineNumber = (int)listener.getUserData(LocationAwareContentHandler.LINE_NUMBER_KEY_NAME);
				hintModel.setLineNumber(lineNumber);
				String contents = FileUtils.readFileToString(fileModel.asFile(), Charset.defaultCharset());
				org.eclipse.jface.text.Document textDoc = new org.eclipse.jface.text.Document(contents);
				IRegion info = textDoc.getLineInformation(lineNumber-1);
				hintModel.setColumnNumber(info.getOffset());
				hintModel.setLength(info.getLength());
				QuickfixModel javaQuickfixModel = createJavaQuickfix(event.getGraphContext(), listenerClassName);
				if (javaQuickfixModel != null) 
				{
					hintModel.addQuickfix(javaQuickfixModel);
				}
			}
		}
	}
	
	private InlineHintModel createHint(GraphRewrite event, GraphService<InlineHintModel> hintService, XmlFileModel fileModel) {
		InlineHintModel hintModel = hintService.create();
        hintModel.setRuleID(XML_LISTENER_RULE_ID);
        hintModel.setTitle(XML_TITLE);
        hintModel.setHint(XML_HINT);
        hintModel.setFile(fileModel);
        hintModel.setEffort(8);
        IssueCategoryModel issueCategoryModel = IssueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.MANDATORY);
        hintModel.setIssueCategory(issueCategoryModel);
        return hintModel;
	}
	
	private static QuickfixModel createXmlQuickfix(GraphContext context, XmlFileModel fileModel) {
		Quickfix quickfix = new Quickfix();
		quickfix.setTransformationID(WeblogicApplicationLifecycleListenerQuickfixTransformation.ID);
		quickfix.setType(QuickfixType.TRANSFORMATION);
		quickfix.setName(XML_QUICKFIX_NAME);
		quickfix.setFileModel(fileModel);
		return quickfix.createQuickfix(context);
	}
	
	private static QuickfixModel createJavaQuickfix(GraphContext context, String clazzName) {
		JavaClassService classService = new JavaClassService(context);
		JavaClassModel classModel =	classService.getByName(clazzName);
		if (classModel != null)
		{
			Quickfix quickfix = new Quickfix();
			quickfix.setTransformationID(WeblogicJavaLifecycleQuickfixTransformation.ID);
			quickfix.setType(QuickfixType.TRANSFORMATION);
			quickfix.setName(JAVA_QUICKFIX_NAME);
			quickfix.setFileModel(classModel.getClassFile());
			return quickfix.createQuickfix(context);
		}
		return null;
	}
}
