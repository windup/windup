package org.jboss.windup.rules.apps.javaee.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationListTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.JPAEntityModel;
import org.jboss.windup.rules.apps.javaee.model.JPANamedQueryModel;
import org.jboss.windup.rules.apps.javaee.service.JPAEntityService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scans for classes with JPA related annotations, and adds JPA related metadata for these.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = AnalyzeJavaFilesRuleProvider.class)
public class DiscoverJPAAnnotationsRuleProvider extends DiscoverAnnotatedClassRuleProvider
{
    private static final Logger LOG = Logging.get(DiscoverJPAAnnotationsRuleProvider.class);

    private static final String ENTITY_ANNOTATIONS = "entityAnnotations";
    private static final String TABLE_ANNOTATIONS_LIST = "tableAnnotations";
    private static final String NAMED_QUERY_LIST = "namedQuery";
    private static final String NAMED_QUERIES_LIST = "namedQueries";
    private static final String DISCRIMINATOR_VALUE_LIST = "discriminatorValueList";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder.begin().addRule()
            .when(JavaClass
                .references("javax.persistence.Entity").at(TypeReferenceLocation.ANNOTATION).as(ENTITY_ANNOTATIONS)
                .or(JavaClass.references("javax.persistence.Table").at(TypeReferenceLocation.ANNOTATION).as(TABLE_ANNOTATIONS_LIST))
                .or(JavaClass.references("javax.persistence.NamedQuery").at(TypeReferenceLocation.ANNOTATION).as(NAMED_QUERY_LIST))
                .or(JavaClass.references("javax.persistence.NamedQueries").at(TypeReferenceLocation.ANNOTATION).as(NAMED_QUERIES_LIST))
                .or(JavaClass.references("javax.persistence.DiscriminatorValue").at(TypeReferenceLocation.ANNOTATION).as(DISCRIMINATOR_VALUE_LIST))
            )
            .perform(Iteration.over(ENTITY_ANNOTATIONS).perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                {
                    extractEntityBeanMetadata(event, payload);
                }
            }).endIteration())
            .withId(ruleIDPrefix + "_JPAEntityBeanRule");
    }

    private JavaAnnotationTypeReferenceModel findTableAnnotation(GraphRewrite event, List<AbstractJavaSourceModel> sourceModels)
    {
        for (AbstractJavaSourceModel sourceModel : sourceModels)
        {
            Optional<JavaAnnotationTypeReferenceModel> tableAnnotation = sourceModel.getAllTypeReferences().stream()
                    .filter(reference -> reference instanceof JavaAnnotationTypeReferenceModel)
                    .map(reference -> (JavaAnnotationTypeReferenceModel)reference)
                    .filter(annotationReference -> annotationReference.getResolvedSourceSnippit() != null && annotationReference.getResolvedSourceSnippit().contains("javax.persistence.Table"))
                    .findFirst();

            return tableAnnotation.orElseGet(() -> findTableAnnotation(event, getParentSourceFiles(event, sourceModel)));
        }
        return null;
    }

    private List<AbstractJavaSourceModel> getParentSourceFiles(GraphRewrite event, AbstractJavaSourceModel sourceModel)
    {
        List<AbstractJavaSourceModel> result = new ArrayList<>();
        if (sourceModel == null)
            return result;

        for (JavaClassModel javaClass : sourceModel.getJavaClasses())
        {
            JavaClassModel parentClass = javaClass.getExtends();
            if (parentClass == null)
                continue;

            AbstractJavaSourceModel parentJavaSourceModel = parentClass.getDecompiledSource();
            if (parentJavaSourceModel == null)
                parentJavaSourceModel = parentClass.getOriginalSource();

            if (parentJavaSourceModel == null)
            {
                LOG.warning("Could not find Java source for class: " + parentClass.getQualifiedName());
                continue;
            }

            result.add(parentJavaSourceModel);
        }
        return result;
    }

    private JavaAnnotationTypeReferenceModel findTableAnnotation(GraphRewrite event, JavaTypeReferenceModel entityTypeReference)
    {
        JavaAnnotationTypeReferenceModel tableAnnotationTypeReference = null;

        final Iterable<? extends WindupVertexFrame> tableAnnotationList = Variables.instance(event).findVariable(TABLE_ANNOTATIONS_LIST);
        if (tableAnnotationList != null)
        {
            for (WindupVertexFrame annotationTypeReferenceBase : tableAnnotationList)
            {
                JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) annotationTypeReferenceBase;
                if (annotationTypeReference.getFile().equals(entityTypeReference.getFile()))
                {
                    tableAnnotationTypeReference = annotationTypeReference;
                    break;
                }
            }
        }

        if (tableAnnotationTypeReference == null)
        {
            AbstractJavaSourceModel sourceModel = entityTypeReference.getFile();
            tableAnnotationTypeReference = findTableAnnotation(event, getParentSourceFiles(event, sourceModel));
        }

        return tableAnnotationTypeReference;
    }

    private void extractEntityBeanMetadata(GraphRewrite event, JavaTypeReferenceModel entityTypeReference)
    {
        LOG.log(Level.INFO, () -> "extractEntityBeanMetadata() with " + entityTypeReference.getDescription());
        entityTypeReference.getFile().setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel entityAnnotationTypeReference = (JavaAnnotationTypeReferenceModel) entityTypeReference;
        JavaAnnotationTypeReferenceModel tableAnnotationTypeReference = findTableAnnotation(event, entityTypeReference);

        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
        JavaClassModel ejbClass = javaClassService.getJavaClass(entityTypeReference);

        String ejbName = getAnnotationLiteralValue(entityAnnotationTypeReference, "name");
        if (ejbName == null)
        {
            ejbName = ejbClass.getClassName();
        }
        String tableName = tableAnnotationTypeReference == null ? ejbName : getAnnotationLiteralValue(tableAnnotationTypeReference, "name");
        if (tableName == null)
        {
            tableName = ejbName;
        }
        String catalogName = tableAnnotationTypeReference == null ? null : getAnnotationLiteralValue(tableAnnotationTypeReference, "catalog");
        String schemaName = tableAnnotationTypeReference == null ? null : getAnnotationLiteralValue(tableAnnotationTypeReference, "schema");

        JPAEntityService jpaService = new JPAEntityService(event.getGraphContext());
        JPAEntityModel jpaEntity = jpaService.create();
        jpaEntity.setApplications(ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), entityTypeReference.getFile().getProjectModel()));
        jpaEntity.setEntityName(ejbName);
        jpaEntity.setJavaClass(ejbClass);
        jpaEntity.setTableName(tableName);
        jpaEntity.setCatalogName(catalogName);
        jpaEntity.setSchemaName(schemaName);

        GraphService<JPANamedQueryModel> namedQueryService = new GraphService<>(event.getGraphContext(), JPANamedQueryModel.class);

        Iterable<? extends WindupVertexFrame> namedQueriesList = Variables.instance(event).findVariable(NAMED_QUERIES_LIST);
        if (namedQueriesList != null)
        {
            for (WindupVertexFrame annotationTypeReferenceBase : namedQueriesList)
            {
                JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) annotationTypeReferenceBase;
                if (!annotationTypeReference.getFile().equals(entityTypeReference.getFile()))
                    continue;

                JavaAnnotationTypeValueModel value = annotationTypeReference.getAnnotationValues().get("value");
                if (value == null || ! (value instanceof JavaAnnotationListTypeValueModel))
                    continue;

                JavaAnnotationListTypeValueModel referenceList = (JavaAnnotationListTypeValueModel) value;
                if (referenceList.getList() != null)
                {
                    for (JavaAnnotationTypeValueModel ref : referenceList.getList())
                    {
                        if (ref instanceof JavaAnnotationTypeReferenceModel)
                        {
                            JavaAnnotationTypeReferenceModel reference = (JavaAnnotationTypeReferenceModel) ref;
                            addNamedQuery(namedQueryService, jpaEntity, reference);
                        }
                        else
                            LOG.warning("Unexpected Annotation in " + ref.toPrettyString());
                    }
                }
            }
        }

        Iterable<? extends WindupVertexFrame> namedQueryList = Variables.instance(event).findVariable(NAMED_QUERY_LIST);
        if (namedQueryList != null)
        {
            for (WindupVertexFrame annotationTypeReferenceBase : namedQueryList)
            {
                JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) annotationTypeReferenceBase;

                if (annotationTypeReference.getFile().equals(entityTypeReference.getFile()))
                {
                    addNamedQuery(namedQueryService, jpaEntity, annotationTypeReference);
                }
            }
        }
    }

    private void addNamedQuery(GraphService<JPANamedQueryModel> namedQueryService, JPAEntityModel jpaEntity,
                JavaAnnotationTypeReferenceModel reference)
    {
        String name = getAnnotationLiteralValue(reference, "name");
        String query = getAnnotationLiteralValue(reference, "query");

        LOG.info("Found query: " + name + " -> " + query);

        JPANamedQueryModel namedQuery = namedQueryService.create();
        namedQuery.setQueryName(name);
        namedQuery.setQuery(query);

        namedQuery.setJpaEntity(jpaEntity);
    }

    @Override
    public String toString()
    {
        return "DiscoverEJBAnnotatedClasses";
    }
}