package org.jboss.windup.reporting.service;

import org.apache.commons.collections.map.LRUMap;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleLifecycleListener;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
class ClassificationServiceCache extends AbstractRuleLifecycleListener implements RuleLifecycleListener {
    /**
     * Keep a cache of items files associated with classification in order to improve performance.
     */
    @SuppressWarnings("unchecked")
    private static synchronized Map<String, Boolean> getCache(GraphRewrite event) {
        Map<String, Boolean> result = (Map<String, Boolean>) event.getRewriteContext().get(ClassificationServiceCache.class);
        if (result == null) {
            result = Collections.synchronizedMap(new LRUMap(30000));
            event.getRewriteContext().put(ClassificationServiceCache.class, result);
        }
        return result;
    }

    /**
     * Indicates whether or not the given {@link FileModel} is already attached to the {@link ClassificationModel}.
     * <p>
     * Note that this assumes all {@link ClassificationModel} attachments are handled via the {@link ClassificationService}.
     * <p>
     * Outside of tests, this should be a safe assumption to make.
     */
    static boolean isClassificationLinkedToFileModel(GraphRewrite event, ClassificationModel classificationModel, FileModel fileModel) {
        String key = getClassificationFileModelCacheKey(classificationModel, fileModel);
        Boolean linked = getCache(event).get(key);

        if (linked == null) {
            GraphTraversal<Vertex, Vertex> existenceCheck = new GraphTraversalSource(event.getGraphContext().getGraph()).V(classificationModel.getElement());
            existenceCheck.out(ClassificationModel.FILE_MODEL);
            existenceCheck.filter(vertexTraverser -> vertexTraverser.get().equals(fileModel.getElement()));

            linked = existenceCheck.hasNext();
            cacheClassificationFileModel(event, classificationModel, fileModel, linked);
        }
        return linked;
    }

    /**
     * Cache the status of the link between the provided {@link ClassificationModel} and the given {@link FileModel}.
     */
    static void cacheClassificationFileModel(GraphRewrite event, ClassificationModel classificationModel, FileModel fileModel, boolean linked) {
        String key = getClassificationFileModelCacheKey(classificationModel, fileModel);
        getCache(event).put(key, linked);
    }

    private static String getClassificationFileModelCacheKey(ClassificationModel classificationModel, FileModel fileModel) {
        StringBuilder builder = new StringBuilder();
        if (classificationModel != null)
            builder.append(classificationModel.getElement().id());
        builder.append("_");
        if (fileModel != null)
            builder.append(fileModel.getElement().id());
        return builder.toString();
    }

    @Override
    public void beforeExecution(GraphRewrite event) {
        getCache(event).clear();
    }

    @Override
    public void afterExecution(GraphRewrite event) {
        getCache(event).clear();
    }
}
