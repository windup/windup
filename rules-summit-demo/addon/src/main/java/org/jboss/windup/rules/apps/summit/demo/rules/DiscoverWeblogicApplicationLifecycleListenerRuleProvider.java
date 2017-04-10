package org.jboss.windup.rules.apps.summit.demo.rules;

import static org.joox.JOOX.$;

import java.util.List;
import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.LocationDataModel;
import org.jboss.windup.reporting.model.QuickfixType;
import org.jboss.windup.reporting.model.TransformationQuickfixModel;
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
@RuleMetadata(phase = InitialAnalysisPhase.class)
public class DiscoverWeblogicApplicationLifecycleListenerRuleProvider extends AbstractRuleProvider
{
    private static Logger LOG = Logger.getLogger(DiscoverWeblogicApplicationLifecycleListenerRuleProvider.class.getName());
    
    private static final String XML_LISTENER_RULE_ID = "weblogic-application-lifecycle-rule-id";
    private static final String XML_TITLE = "Weblogic application lifecycle listener.";
    private static final String XML_HINT = "Application lifecycle listeners registered within weblogic-appplication.xml need to be refactored.";
    private static final String XML_HINT_DESCRIPTION = "Remove the <listener>...</listener> tag";
	
	@Override
	public Configuration getConfiguration(RuleLoaderContext context) 
	{
		return ConfigurationBuilder.begin()
				.addRule()
				.when(Query.fromType(XmlFileModel.class).withProperty(FileModel.FILE_NAME, "weblogic-application.xml")
						.withProperty(XmlFileModel.ROOT_TAG_NAME, "weblogic-application"))
				.perform(new AbstractIterationOperation<XmlFileModel>() {
					 public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel fileModel) {
						 createHints(event, context, fileModel);
					 }
				}).withId(XML_LISTENER_RULE_ID);
	}
	
	private void createHints(GraphRewrite event, EvaluationContext context, XmlFileModel fileModel)
	{
		GraphService<InlineHintModel> hintService = new GraphService<>(event.getGraphContext(), InlineHintModel.class);
		GraphService<WeblogicXmlApplicationLifecycleChangeModel> weblogicXmlService = new GraphService<>(event.getGraphContext(), WeblogicXmlApplicationLifecycleChangeModel.class);
		GraphService<LocationDataModel> locationService = new GraphService<>(event.getGraphContext(), LocationDataModel.class);
		
		Document document = fileModel.asDocument();
		List<Element> listeners = $(document).find("listener-class").get();
		for (Element listener : listeners) {
			InlineHintModel hintModel = createHint(event, hintService, fileModel);
	        TransformationQuickfixModel quickfixModel = createQuickfix(event, hintModel);
	        quickfixModel.setQuickfixType(QuickfixType.TRANSFORMATION);
	        quickfixModel.setReplacement("");
	        quickfixModel.setReplacement("");
	        quickfixModel.setSearch("");
	        WeblogicXmlApplicationLifecycleChangeModel xmlChangeModel = 
	        		createXmlQuickfixChange(event, context, fileModel, hintModel, quickfixModel, weblogicXmlService, locationService, listener);
	        hintModel.setLineNumber(xmlChangeModel.getLocation().getStartLine());
	        hintModel.setColumnNumber(xmlChangeModel.getLocation().getStartColumn());
	        hintModel.setLength(0);
		}
	}
	
	private InlineHintModel createHint(GraphRewrite event, GraphService<InlineHintModel> hintService, XmlFileModel fileModel) {
		InlineHintModel hintModel = hintService.create();
        hintModel.setRuleID(XML_LISTENER_RULE_ID);
        //hintModel.setFileLocationReference(fileModel);
        hintModel.setHint(XML_HINT);
        hintModel.setFile(fileModel);
        hintModel.setEffort(8);

        IssueCategoryModel issueCategoryModel = IssueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.MANDATORY);
        hintModel.setIssueCategory(issueCategoryModel);
        return hintModel;
	}
	
	private TransformationQuickfixModel createQuickfix(GraphRewrite event, InlineHintModel hintModel)
	{
		GraphService<TransformationQuickfixModel> service = new GraphService<>(event.getGraphContext(), TransformationQuickfixModel.class);
		TransformationQuickfixModel quickfixModel = service.create();
		hintModel.addQuickfix(quickfixModel);
		quickfixModel.setHintModel(hintModel);
		return quickfixModel;
	}
	
	private WeblogicXmlApplicationLifecycleChangeModel createXmlQuickfixChange(GraphRewrite event, EvaluationContext context, XmlFileModel fileModel, InlineHintModel hintModel, 
			TransformationQuickfixModel quickfixModel, GraphService<WeblogicXmlApplicationLifecycleChangeModel> weblogicXmlService,
			GraphService<LocationDataModel> locationService, Element listener)
	{
		WeblogicXmlApplicationLifecycleChangeModel changeModel = weblogicXmlService.create();
		LocationDataModel locationModel = locationService.create();
		locationModel.setStartLine((int)listener.getUserData(LocationAwareContentHandler.LINE_NUMBER_KEY_NAME));
		locationModel.setStartColumnn(0);
		locationModel.setEndLine(0);
		locationModel.setEndColumnn(0);
		quickfixModel.addChange(changeModel);
		changeModel.setLocation(locationModel);
		changeModel.setFile(fileModel);
		changeModel.setTitle("Weblogic proprietary application lifecycle listener.");
		changeModel.setDescription("Remove <listener-class>");
		return changeModel;
	}
}
