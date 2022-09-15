package org.jboss.windup.rules.apps.javaee.rules.datasource;

import org.jboss.forge.furnace.util.Strings;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.Set;

/**
 * Ruleset to discover data sources in annotations
 * <p>
 * https://docs.oracle.com/cd/E24329_01/web.1211/e24376/ds_annotation.htm#JDBCP1043
 *
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = AnalyzeJavaFilesRuleProvider.class)
public class DiscoverDataSourceAnnotationRuleProvider extends AbstractRuleProvider {

    @Override
    public Configuration getConfiguration(RuleLoaderContext context) {
        String ruleIDPrefix = getClass().getSimpleName();

        return ConfigurationBuilder.begin()
                .addRule()
                .when(JavaClass.references("javax.annotation.sql.DataSourceDefinition").at(TypeReferenceLocation.ANNOTATION))
                .perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                        extractDataSourceMetadata(event, payload);
                    }
                })
                .withId(ruleIDPrefix + "_DataSourceDefinition");
    }

    private String getAnnotationLiteralValue(JavaAnnotationTypeReferenceModel model, String name) {
        JavaAnnotationTypeValueModel valueModel = model.getAnnotationValues().get(name);

        if (valueModel instanceof JavaAnnotationLiteralTypeValueModel) {
            JavaAnnotationLiteralTypeValueModel literalTypeValue = (JavaAnnotationLiteralTypeValueModel) valueModel;
            return literalTypeValue.getLiteralValue();
        } else {
            return null;
        }
    }

    private void extractDataSourceMetadata(GraphRewrite event, JavaTypeReferenceModel javaTypeReference) {
        javaTypeReference.getFile().setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) javaTypeReference;

        JavaClassModel datasourceClass = getJavaClass(javaTypeReference);

        String dataSourceName = getAnnotationLiteralValue(annotationTypeReference, "name");
        if (Strings.isNullOrEmpty(dataSourceName)) {
            dataSourceName = datasourceClass.getClassName();
        }

        String isXaString = getAnnotationLiteralValue(annotationTypeReference, "transactional");

        boolean isXa = isXaString == null || Boolean.getBoolean(isXaString);

        Service<DataSourceModel> dataSourceService = new GraphService<>(event.getGraphContext(), DataSourceModel.class);
        DataSourceModel dataSourceModel = dataSourceService.create();
        Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), javaTypeReference.getFile().getProjectModel());

        dataSourceModel.setApplications(applications);
        dataSourceModel.setName(dataSourceName);
        dataSourceModel.setXa(isXa);
        dataSourceModel.setJndiLocation(dataSourceName);
    }

    private JavaClassModel getJavaClass(JavaTypeReferenceModel javaTypeReference) {
        JavaClassModel result = null;
        AbstractJavaSourceModel javaSource = javaTypeReference.getFile();
        for (JavaClassModel javaClassModel : javaSource.getJavaClasses()) {
            // there can be only one public one, and the annotated class should be public
            if (javaClassModel.isPublic() != null && javaClassModel.isPublic()) {
                result = javaClassModel;
                break;
            }
        }

        if (result == null) {
            // no public classes found, so try to find any class (even non-public ones)
            result = javaSource.getJavaClasses().iterator().next();
        }
        return result;
    }

    @Override
    public String toString() {
        return "DiscoverDataSource";
    }
}
