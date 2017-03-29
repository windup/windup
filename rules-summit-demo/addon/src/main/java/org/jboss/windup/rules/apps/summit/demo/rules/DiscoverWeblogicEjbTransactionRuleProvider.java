package org.jboss.windup.rules.apps.summit.demo.rules;

import static org.joox.JOOX.$;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

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

import com.google.common.collect.Lists;

/**
 * Discovers usage of weblogic lazy DB transactions configured within weblogic-ejb-jar.xml.
 */
@RuleMetadata(after = MigrationRulesPhase.class)
public class DiscoverWeblogicEjbTransactionRuleProvider extends AbstractRuleProvider {

	private static Logger LOG = Logger.getLogger(DiscoverWeblogicEjbTransactionRuleProvider.class.getName());

	private static final String XML_LISTENER_RULE_ID = "weblogic-lazy-DB-transaction-rule-id";
	private static final String XML_TITLE = "Weblogic lazy DB transaction.";
	private static final String XML_HINT = "The same behavior can be achieved by specifying the <sync-on-commit-only> in the jbosscmp-jdbc.xml file.";
	private static final String XML_QUICKFIX_NAME = "Remove the <delay-updates-until-end-of-tx> tag.";
	private static final String JBOSS_JDBC_QUICKFIX_NAME = "Add <sync-on-commit-only>false</sync-on-commit-only> to the corresponding entity within jbosscmp-jdbc.";

	@Override
	public Configuration getConfiguration(RuleLoaderContext context) {
		return ConfigurationBuilder.begin().addRule()
				.when(Query.fromType(XmlFileModel.class).withProperty(FileModel.FILE_NAME, "weblogic-ejb-jar.xml")
						.withProperty(XmlFileModel.ROOT_TAG_NAME, "weblogic-ejb-jar"))
				.perform(new AbstractIterationOperation<XmlFileModel>() {
					public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel fileModel) {
						try {
							createHints(event, context, fileModel);
						} catch (Exception e) {
							LOG.severe(e.getMessage());
						}
					}
				}).withId(XML_LISTENER_RULE_ID);
	}

	private void createHints(GraphRewrite event, EvaluationContext context, XmlFileModel fileModel) throws Exception {
		GraphService<InlineHintModel> hintService = new GraphService<>(event.getGraphContext(), InlineHintModel.class);

		Document document = fileModel.asDocument();
		List<Element> updateElements = $(document).find("delay-updates-until-end-of-tx").get();
		
		for (Element updateElement : updateElements) {
			int lineNumber = (int) updateElement.getUserData(LocationAwareContentHandler.LINE_NUMBER_KEY_NAME);
			String value = updateElement.getTextContent();
			if (StringUtils.isNotBlank(value) && Boolean.valueOf(value) == false) {
				InlineHintModel hintModel = createHint(event, hintService, fileModel);
				hintModel.addQuickfix(createWeblogicXmlQuickfix(event.getGraphContext(), fileModel));
				hintModel.setLineNumber(lineNumber);

				String contents = FileUtils.readFileToString(fileModel.asFile(), Charset.defaultCharset());
				org.eclipse.jface.text.Document textDoc = new org.eclipse.jface.text.Document(contents);

				IRegion info = textDoc.getLineInformation(lineNumber - 1);
				hintModel.setColumnNumber(info.getOffset());
				hintModel.setLength(info.getLength());
				
				Element beanNode = (Element)updateElement.getParentNode().getParentNode().getParentNode();
				
				Element nameElement = $(beanNode).child("ejb-name").get(0);
				String entityName = StringUtils.trimToNull(nameElement.getTextContent());
				
				QuickfixModel javaQuickfixModel = createJBossQuickfix(event.getGraphContext(), fileModel, entityName);
				if (javaQuickfixModel != null) {
					hintModel.addQuickfix(javaQuickfixModel);
				}
			}
		}
	}

	private InlineHintModel createHint(GraphRewrite event, GraphService<InlineHintModel> hintService,
			XmlFileModel fileModel) {
		InlineHintModel hintModel = hintService.create();
		hintModel.setRuleID(XML_LISTENER_RULE_ID);
		hintModel.setTitle(XML_TITLE);
		hintModel.setHint(XML_HINT);
		hintModel.setFile(fileModel);
		hintModel.setEffort(8);
		IssueCategoryModel issueCategoryModel = IssueCategoryRegistry.loadFromGraph(event.getGraphContext(),
				IssueCategoryRegistry.MANDATORY);
		hintModel.setIssueCategory(issueCategoryModel);
		return hintModel;
	}

	private static QuickfixModel createWeblogicXmlQuickfix(GraphContext context, XmlFileModel fileModel) {
		Quickfix quickfix = new Quickfix();
		quickfix.setTransformationID(WeblogicEjbTransactionTransformationQuickfix.ID);
		quickfix.setType(QuickfixType.TRANSFORMATION);
		quickfix.setName(XML_QUICKFIX_NAME);
		quickfix.setFileModel(fileModel);
		return quickfix.createQuickfix(context);
	}

	private static QuickfixModel createJBossQuickfix(GraphContext context, XmlFileModel ejbFileModel, String clazzName) {
		Optional<FileModel> fileModel = StreamSupport.stream(ejbFileModel.getProjectModel().getFileModels().spliterator(), false)
			.filter(model -> model.getFileName().equals("jbosscmp-jdbc.xml")).findFirst();
		if (fileModel.isPresent()) {
			XmlFileModel jbossFileModel = (XmlFileModel)fileModel.get();
			Quickfix quickfix = new Quickfix();
			quickfix.setTransformationID(JBossEjbTransactionTransformationQuickfix.ID);
			quickfix.setType(QuickfixType.TRANSFORMATION);
			quickfix.setName(JBOSS_JDBC_QUICKFIX_NAME);
			quickfix.setFileModel(jbossFileModel);
			return quickfix.createQuickfix(context);
		}
		return null;
	}
}
