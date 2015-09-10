package org.jboss.windup.reporting.service;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleLifecycleListener;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
class ClassificationServiceCache extends AbstractRuleLifecycleListener implements RuleLifecycleListener
{
    /**
     * Keep a cache of items files associated with classification in order to improve performance.
     */
    private static Map<String, Boolean> classificationFileModelCache = Collections.synchronizedMap(new LRUMap(30000));

    /**
     * Indicates whether or not the given {@link FileModel} is already attached to the {@link ClassificationModel}.
     *
     * Note that this assumes all {@link ClassificationModel} attachments are handled via the {@link ClassificationService}.
     *
     * Outside of tests, this should be a safe assumption to make.
     */
    static boolean isClassificationLinkedToFileModel(ClassificationModel classificationModel, FileModel fileModel)
    {
        String key = getClassificationFileModelCacheKey(classificationModel, fileModel);
        Boolean linked = classificationFileModelCache.get(key);

        if (linked == null)
        {
            GremlinPipeline<Vertex, Vertex> existenceCheck = new GremlinPipeline<>(classificationModel.asVertex());
            existenceCheck.out(ClassificationModel.FILE_MODEL);
            existenceCheck.retain(Collections.singleton(classificationModel.asVertex()));

            linked = existenceCheck.iterator().hasNext();
            cacheClassificationFileModel(classificationModel, fileModel, linked);
        }
        return linked;
    }

    /**
     * Cache the status of the link between the provided {@link ClassificationModel} and the given {@link FileModel}.
     */
    static void cacheClassificationFileModel(ClassificationModel classificationModel, FileModel fileModel, boolean linked)
    {
        String key = getClassificationFileModelCacheKey(classificationModel, fileModel);
        classificationFileModelCache.put(key, linked);
    }

    private static String getClassificationFileModelCacheKey(ClassificationModel classificationModel, FileModel fileModel)
    {
        StringBuilder builder = new StringBuilder();
        if (classificationModel != null)
            builder.append(classificationModel.asVertex().getId());
        builder.append("_");
        if (fileModel != null)
            builder.append(fileModel.asVertex().getId());
        return builder.toString();
    }

    @Override
    public void beforeExecution(GraphRewrite event)
    {
        classificationFileModelCache.clear();
    }

    @Override
    public void afterExecution(GraphRewrite event)
    {
        classificationFileModelCache.clear();
    }
}
