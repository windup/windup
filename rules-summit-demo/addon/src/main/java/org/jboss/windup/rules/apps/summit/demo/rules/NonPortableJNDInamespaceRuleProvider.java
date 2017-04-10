package org.jboss.windup.rules.apps.summit.demo.rules;

import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.QuickfixModel;
import org.jboss.windup.reporting.model.QuickfixType;
import org.jboss.windup.reporting.quickfix.Quickfix;
import org.jboss.windup.rules.files.condition.FileContent;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers usage of JNDI.
 */
@RuleMetadata(after = MigrationRulesPhase.class)
public class NonPortableJNDInamespaceRuleProvider extends AbstractRuleProvider
{
    private static Logger LOG = Logger.getLogger(NonPortableJNDInamespaceRuleProvider.class.getName());
    
    private static final String RULE_ID = "non-portable-JNDI-namespace";
    private static final String HINT_TITLE = "Non-portable JNDI namepsace reference.";
    private static final String HINT = "InitialContext should be instantiated with no arguments. Once an instance is constructed, look up the service using portable JNDI lookup syntax. Ensure also that in case system properties for InitialContext are provided, they do not need to be changed for the JBoss."; 
    private static final String QUICKFIX_NAME = "InitialContext should be instantiated with no arguments. Once an instance is constructed, look up the service using portable JNDI lookup syntax";
    
	@Override
	public Configuration getConfiguration(RuleLoaderContext context) 
	{
		return ConfigurationBuilder
		        .begin()
		        .addRule()
		        .when(FileContent.matches("Service service = (Service)context.lookup("))
		        .perform(new AbstractIterationOperation<FileLocationModel>()
		        {
		            public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel fileModel)
		            {
		            	try
		            	{
		            		createHints(event, context, fileModel);
		            	}
		            	catch (Exception e)
						 {
							 LOG.severe(e.getMessage());
						 }
		            	
		            }
		        })
		        .withId(RULE_ID);
	}
	
	private void createHints(GraphRewrite event, EvaluationContext context, FileLocationModel fileLocationModel) throws Exception
	{
		GraphService<InlineHintModel> hintService = new GraphService<>(event.getGraphContext(), InlineHintModel.class);
		FileModel fileModel = fileLocationModel.getFile();
		InlineHintModel hintModel = createHint(event, hintService, fileModel);
		hintModel.addQuickfix(createQuickfix(event.getGraphContext(), fileModel));
		hintModel.setLineNumber(fileLocationModel.getLineNumber());
		hintModel.setColumnNumber(0);
		hintModel.setLength(0);
	}
	
	private InlineHintModel createHint(GraphRewrite event, GraphService<InlineHintModel> hintService, FileModel fileModel) {
		InlineHintModel hintModel = hintService.create();
        hintModel.setRuleID(RULE_ID);
        hintModel.setTitle(HINT_TITLE);
        hintModel.setHint(HINT);
        hintModel.setFile(fileModel);
        hintModel.setEffort(8);
        IssueCategoryModel issueCategoryModel = IssueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.MANDATORY);
        hintModel.setIssueCategory(issueCategoryModel);
        return hintModel;
	}
	
	private static QuickfixModel createQuickfix(GraphContext context, FileModel fileModel) {
		Quickfix quickfix = new Quickfix();
		quickfix.setTransformationID(JNDILookupQuickfixTransformation.ID);
		quickfix.setType(QuickfixType.TRANSFORMATION);
		quickfix.setName(QUICKFIX_NAME);
		quickfix.setFileModel(fileModel);
		return quickfix.createQuickfix(context);
	}
}
