package org.jboss.windup.rules.apps.javaee.rules.datasource;

import static org.joox.JOOX.$;

import java.util.Arrays;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.service.DataSourceService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Rule for identifying datasources in *-ds.xml files
 *
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = AnalyzeJavaFilesRuleProvider.class, perform = "Discover datasources")
public class DiscoverDataSourceDsXmlRuleProvider extends IteratingRuleProvider<XmlFileModel> {
    private static final String DATASOURCES_ROOT_TAG = "datasources";
    private static final String SINGLE_DATASOURCE_TAG = "datasource";
    private static final String SINGLE_DATASOURCE_XA_TAG = "xa-datasource";

    @Override
    public ConditionBuilder when() {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, DATASOURCES_ROOT_TAG);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload) {
        createDataSourceModel(event, context, payload);
    }

    private void createDataSourceModel(GraphRewrite event, EvaluationContext context, XmlFileModel xmlFileModel) {
        GraphContext graphContext = event.getGraphContext();
        DataSourceService dataSourceService = new DataSourceService(graphContext);

        // check the root XML node.
        Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), xmlFileModel.getProjectModel());

        Document doc = new XmlFileService(graphContext).loadDocumentQuiet(event, context, xmlFileModel);

        for (String tagName : Arrays.asList(SINGLE_DATASOURCE_TAG, SINGLE_DATASOURCE_XA_TAG)) {
            for (Element element : $(doc).find(tagName).get()) {
                DataSourceModel dataSourceModel = dataSourceService.create();

                boolean isXa = tagName.equals(SINGLE_DATASOURCE_XA_TAG);
                dataSourceModel.setName(element.getAttribute("pool-name"));
                dataSourceModel.setJndiLocation(element.getAttribute("jndi-name"));
                dataSourceModel.setApplications(applications);
                dataSourceModel.setXa(isXa);
            }
        }
    }
}
