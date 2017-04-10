package org.jboss.windup.rules.apps.summit.demo.rules;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceTagType;
import org.jboss.windup.rules.apps.javaee.service.EnvironmentReferenceService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.util.xml.LocationAwareContentHandler;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Discovers Weblogic Application Lifecycle Listeners registered within an weblogic-application.xml file.
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, perform = "Discover WebLogic Application Lifecycle Listeners")
public class ResolveWeblogicLifecycleListenerRuleProvider extends IteratingRuleProvider<XmlFileModel>
{

    private static final Logger LOG = Logger.getLogger(ResolveWeblogicLifecycleListenerRuleProvider.class.getSimpleName());

	@Override
	public ConditionBuilder when() 
	{
        return Query.fromType(XmlFileModel.class).withProperty(FileModel.FILE_NAME, "weblogic-application.xml")
        			.withProperty(XmlFileModel.ROOT_TAG_NAME, "weblogic-application");
	}

	@Override
	public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel sourceFile) 
	{
        InlineHintService hintService = new InlineHintService(event.getGraphContext());
        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
		XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        Document doc = xmlFileService.loadDocumentQuiet(event, context, sourceFile);

        for (Element listenerRef : $(doc).find("listener").get())
        {
        	for (Element listenerClassRef : $(listenerRef).child("listener-class").get())
        	{
        		JavaClassModel classModel = findClassModel(javaClassService, listenerClassRef.getNodeValue());
        		if (classModel != null) 
        		{
        			createListenerClassHint(hintService, context, listenerClassRef, classModel, sourceFile);
        		}
        	}
        }
	}
	
	private JavaClassModel findClassModel(JavaClassService classService, String listenerClazz)
	{
		JavaClassModel classModel = null;
		if (StringUtils.isNotBlank(listenerClazz))
		{
			classModel = classService.getByName(listenerClazz);
		}
		return classModel;
	}
	
	private void createListenerClassHint(InlineHintService hintService, EvaluationContext context, Element listenerClassRef, JavaClassModel classModel, XmlFileModel sourceFile) 
	{
        /*InlineHintModel hintModel = hintService.create();
        hintModel.setRuleID(((Rule) context.get(Rule.class)).getId());
        hintModel.setFile(sourceFile);
        hintModel.setEffort(1);
        
        int lineNumber = (int) listenerClassRef.getUserData(LocationAwareContentHandler.LINE_NUMBER_KEY_NAME);
        hintModel.setLineNumber(lineNumber);
        
        IssueCategoryRegistry issueCategoryRegistry = IssueCategoryRegistry.instance(context);
         hintModel.setIssueCategory(issueCategoryRegistry.loadFromGraph(context, IssueCategoryRegistry.MANDATORY));
        hintModel.setHint("Weblogic Application Lifecycle Listener");
        */
	}
}
