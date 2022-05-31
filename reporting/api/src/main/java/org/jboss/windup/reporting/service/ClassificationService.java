package org.jboss.windup.reporting.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.attribute.Text;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.frames.FramedVertexIterable;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.EffortReportModel;
import org.jboss.windup.reporting.model.IssueDisplayMode;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adds methods for loading and querying ClassificationModel related data.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ClassificationService extends GraphService<ClassificationModel> {
    public static final Logger LOG = Logger.getLogger(ClassificationService.class.getName());

    public ClassificationService(GraphContext context) {
        super(context, ClassificationModel.class);
    }

    /**
     * Returns the total effort points in all of the {@link ClassificationModel}s associated with the provided {@link FileModel}.
     */
    public int getMigrationEffortPoints(FileModel fileModel) {
        GraphTraversal<Vertex, Vertex> classificationPipeline = new GraphTraversalSource(getGraphContext().getGraph()).V(fileModel.getElement());
        classificationPipeline.in(ClassificationModel.FILE_MODEL);
        classificationPipeline.has(EffortReportModel.EFFORT, P.gt(0));
        classificationPipeline.has(WindupVertexFrame.TYPE_PROP, Text.textContains(ClassificationModel.TYPE));

        int classificationEffort = 0;
        for (Vertex v : classificationPipeline.toList()) {
            Property<Integer> migrationEffort = v.property(ClassificationModel.EFFORT);
            if (migrationEffort.isPresent()) {
                classificationEffort += migrationEffort.value();
            }
        }
        return classificationEffort;
    }

    /**
     * Return all {@link ClassificationModel} instances that are attached to the given {@link FileModel} instance.
     */
    public Iterable<ClassificationModel> getClassifications(FileModel model) {
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V(model.getElement());
        pipeline.in(ClassificationModel.FILE_MODEL);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.textContains(ClassificationModel.TYPE));
        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline.toList(), ClassificationModel.class);
    }

    /**
     * Return all {@link ClassificationModel} instances that are attached to the given {@link FileModel} instance with a specific classification name.
     */
    public Iterable<ClassificationModel> getClassificationByName(FileModel model, String classificationName) {
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V(model.getElement());
        pipeline.in(ClassificationModel.FILE_MODEL);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.textContains(ClassificationModel.TYPE));
        pipeline.has(ClassificationModel.CLASSIFICATION, classificationName);
        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline.toList(), ClassificationModel.class);
    }

    /**
     * <p>
     * Returns the total effort points in all of the {@link ClassificationModel}s
     * associated with the {@link FileModel} instances in the given {@link ProjectModelTraversal}.
     * </p>
     * <p>
     * If set to recursive, then also include the effort points from child projects.
     * </p>
     * <p>
     * The result is a Map, the key contains the effort level and the value contains the number of incidents.
     * </p>
     */
    public Map<Integer, Integer> getMigrationEffortByPoints(ProjectModelTraversal traversal, Set<String> includeTags, Set<String> excludeTags,
                                                            Set<String> issueCategoryIDs,
                                                            boolean recursive, boolean includeZero) {
        MapSumEffortAccumulatorFunction<Integer> accumulator = new MapSumEffortAccumulatorFunction() {
            public Integer vertexToKey(Vertex effortReportVertex) {
                Integer migrationEffort = (Integer) effortReportVertex.property(EffortReportModel.EFFORT).value();
                return migrationEffort;
            }
        };
        getMigrationEffortDetails(traversal, includeTags, excludeTags, issueCategoryIDs, recursive, includeZero, accumulator);
        return accumulator.getResults();
    }

    /**
     * Returns the total incidents in all of the {@link ClassificationModel}s associated with the files in this project by severity.
     */
    public Map<IssueCategoryModel, Integer> getMigrationEffortBySeverity(GraphRewrite event,
                                                                         ProjectModelTraversal traversal, Set<String> includeTags, Set<String> excludeTags, Set<String> issueCategoryIDs, boolean recursive) {
        MapSumEffortAccumulatorFunction<IssueCategoryModel> accumulator = new MapSumEffortAccumulatorFunction<IssueCategoryModel>() {
            public IssueCategoryModel vertexToKey(Vertex effortReportVertex) {
                return frame(effortReportVertex).getIssueCategory();
            }

            @Override
            public void accumulate(Vertex effortReportVertex) {
                /*
                 * If it is a detail only issue, then summaries should not include it in the count.
                 */
                if (frame(effortReportVertex).getIssueDisplayMode() == IssueDisplayMode.DETAIL_ONLY)
                    return;

                super.accumulate(effortReportVertex);
            }
        };
        this.getMigrationEffortDetails(traversal, includeTags, excludeTags, issueCategoryIDs, recursive, true, accumulator);
        return accumulator.getResults();
    }

    private void getMigrationEffortDetails(ProjectModelTraversal traversal, Set<String> includeTags, Set<String> excludeTags,
                                           Set<String> issueCategoryIDs, boolean recursive, boolean includeZero,
                                           EffortAccumulatorFunction accumulatorFunction) {
        LOG.log(Level.INFO, String.format(System.lineSeparator() + "\t\t\tEFFORT C: getMigrationEffortDetails() with: %s, %srecur, %sincludeZero, %s, tags: %s, excl: %s",
                traversal, recursive ? "" : "!", includeZero ? "" : "!", accumulatorFunction, includeTags, excludeTags));

        final Set<Vertex> initialVertices = traversal.getAllProjectsAsVertices(recursive);

        GraphTraversal<Vertex, Vertex> pipeline = this.getGraphContext().getGraph().traversal().V();
        // If the multivalue index is not 1st, then it doesn't work - https://github.com/thinkaurelius/titan/issues/403
        if (!includeZero) {
            pipeline.has(EffortReportModel.EFFORT, P.gt(0));
            pipeline.has(WindupVertexFrame.TYPE_PROP, P.eq(ClassificationModel.TYPE));
        } else {
            pipeline.has(WindupVertexFrame.TYPE_PROP, ClassificationModel.TYPE);
        }
        pipeline.as("classification");
        // For each classification, count it repeatedly for each file that is within given set of Projects (from the traversal).
        pipeline.out(ClassificationModel.FILE_MODEL);
        pipeline.in(ProjectModel.PROJECT_MODEL_TO_FILE);
        pipeline.filter(new SetMembersFilter(initialVertices));
        pipeline.select("classification");

        boolean checkTags = !includeTags.isEmpty() || !excludeTags.isEmpty();
        FileService fileService = new FileService(getGraphContext());
        for (Vertex v : pipeline.toSet()) {
            if (checkTags || !issueCategoryIDs.isEmpty()) {
                ClassificationModel classificationModel = frame(v);

                // only check tags if we have some passed in
                if (checkTags && !classificationModel.matchesTags(includeTags, excludeTags))
                    continue;

                if (!issueCategoryIDs.isEmpty() && !issueCategoryIDs.contains(classificationModel.getIssueCategory().getCategoryID()))
                    continue;
            }

            // For each classification, count it repeatedly for each file.
            // TODO: .accumulate(v, count);
            // TODO: This could be all done just within the query (provided that the tags would be taken care of).
            //       Accumulate could be a PipeFunction.
            Iterator<Vertex> fileVertexIterator = v.vertices(Direction.OUT, ClassificationModel.FILE_MODEL);
            while (fileVertexIterator.hasNext()) {
                Vertex fileVertex = fileVertexIterator.next();

                // Make sure that this file is actually in an accepted project. The pipeline condition will return
                // classifications that aren't necessarily in the same project.
                FileModel fileModel = fileService.frame(fileVertex);
                if (initialVertices.contains(fileModel.getProjectModel().getElement()))
                    accumulatorFunction.accumulate(v);
            }
        }
    }

    /**
     * Attach a {@link ClassificationModel} with the given classificationText and description to the provided {@link FileModel}.
     * If an existing Model exists with the provided classificationText, that one will be used instead.
     */
    public ClassificationModel attachClassification(GraphRewrite event, Rule rule, FileModel fileModel, String classificationText, String description) {
        return attachClassification(event, rule, fileModel, IssueCategoryRegistry.DEFAULT, classificationText, description);
    }

    /**
     * Attach a {@link ClassificationModel} with the given classificationText and description to the provided {@link FileModel}.
     * If an existing Model exists with the provided classificationText, that one will be used instead.
     * <p>
     * The classification is looked up by name and created only if not found.
     * That means that the new description is discarded. FIXME
     */
    public ClassificationModel attachClassification(GraphRewrite event, Rule rule, FileModel fileModel, String categoryId, String classificationTitle, String description) {
        Traversal<?, ?> classificationTraversal = getQuery().traverse(g -> g.has(ClassificationModel.CLASSIFICATION, classificationTitle)).getRawTraversal();
        ClassificationModel classification = getUnique(classificationTraversal);
        if (classification == null) {
            classification = create();
            classification.setClassification(classificationTitle);
            classification.setDescription(description);
            classification.setEffort(0);

            IssueCategoryModel cat = IssueCategoryRegistry.loadFromGraph(event.getGraphContext(), categoryId);
            classification.setIssueCategory(cat);

            classification.setRuleID(rule.getId());
            if (fileModel instanceof DuplicateArchiveModel) {
                fileModel = ((DuplicateArchiveModel) fileModel).getCanonicalArchive();
            }
            classification.addFileModel(fileModel);
            if (fileModel instanceof SourceFileModel)
                ((SourceFileModel) fileModel).setGenerateSourceReport(true);

            ClassificationServiceCache.cacheClassificationFileModel(event, classification, fileModel, true);
            return classification;
        } else {
            if (!StringUtils.equals(description, classification.getDescription()))
                LOG.warning("The description of the newly attached classification differs from the same-titled existing one, so the old description is being changed."
                        + System.lineSeparator() + "   Clsf title: " + classification.getClassification()
                        + System.lineSeparator() + "   Old desc: " + classification.getDescription()
                        + System.lineSeparator() + "   New desc: " + description);
            classification.setDescription(description);
        }

        return attachClassification(event, classification, fileModel);
    }

    /**
     * Attach a {@link ClassificationModel} with the given classificationText and description to the provided {@link FileModel}.
     * If an existing Model exists with the provided classificationText, that one will be used instead.
     */
    public ClassificationModel attachClassification(GraphRewrite event, EvaluationContext context, FileModel fileModel, String classificationText, String description) {
        return attachClassification(event, context, fileModel, IssueCategoryRegistry.DEFAULT, classificationText, description);
    }

    public ClassificationModel attachClassification(GraphRewrite event, EvaluationContext context, FileModel fileModel, String categoryId, String classificationText, String description) {
        Rule rule = (Rule) context.get(Rule.class);
        return attachClassification(event, rule, fileModel, categoryId, classificationText, description);
    }

    private boolean isClassificationLinkedToFileModel(GraphRewrite event, ClassificationModel classificationModel, FileModel fileModel) {
        return ClassificationServiceCache.isClassificationLinkedToFileModel(event, classificationModel, fileModel);
    }

    /**
     * This method just attaches the {@link ClassificationModel} to the {@link FileModel}.
     * It will only do so if this link is not already present.
     */
    public ClassificationModel attachClassification(GraphRewrite event, ClassificationModel classificationModel, FileModel fileModel) {
        if (fileModel instanceof DuplicateArchiveModel) {
            fileModel = ((DuplicateArchiveModel) fileModel).getCanonicalArchive();
        }

        if (!isClassificationLinkedToFileModel(event, classificationModel, fileModel)) {
            classificationModel.addFileModel(fileModel);
            if (fileModel instanceof SourceFileModel)
                ((SourceFileModel) fileModel).setGenerateSourceReport(true);
        }
        ClassificationServiceCache.cacheClassificationFileModel(event, classificationModel, fileModel, true);

        return classificationModel;
    }

    /**
     * Attach the given link to the classification, while checking for duplicates.
     */
    public ClassificationModel attachLink(ClassificationModel classificationModel, LinkModel linkModel) {
        for (LinkModel existing : classificationModel.getLinks()) {
            if (StringUtils.equals(existing.getLink(), linkModel.getLink())) {
                return classificationModel;
            }
        }
        classificationModel.addLink(linkModel);
        return classificationModel;
    }
}
