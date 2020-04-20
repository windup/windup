package org.jboss.windup.reporting.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleLifecycleListener;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.IssueDisplayMode;

class InlineHintServiceCache extends AbstractRuleLifecycleListener implements RuleLifecycleListener
{
    /**
     * Keep a cache of items files associated with classification in order to improve performance.
     */
    @SuppressWarnings("unchecked")
    private static synchronized Map<Set<String>, Vertex> getCache(GraphRewrite event)
    {
        Map<Set<String>, Vertex> result = (Map<Set<String>, Vertex>) event.getRewriteContext().get(InlineHintServiceCache.class);
        if (result == null)
        {
            result = Collections.synchronizedMap(new HashMap<>());
            event.getRewriteContext().put(InlineHintServiceCache.class, result);
        }
        return result;
    }

    /**
     * Indicates whether or not the given {@link FileLocationModel} is already attached to the {@link InlineHintModel}.
     *
     * Note that this assumes all {@link InlineHintModel} attachments are handled via the {@link ClassificationService}.
     *
     * Outside of tests, this should be a safe assumption to make.
     */
/*
    static boolean hintAlreadyAdded(GraphRewrite event, String ruleId, String hintMessage, String hintTitle, int effort,
                IssueDisplayMode issueDisplayMode, FileLocationModel fileLocationModel)
    {
        Set<String> key = getCacheKey(ruleId, hintMessage, hintTitle, effort, issueDisplayMode, fileLocationModel);
        return getCache(event).containsKey(key);
    }
*/

    static boolean hintAlreadyAdded(GraphRewrite event, String ruleId, String hintMessage, String hintTitle, FileLocationModel fileLocationModel)
    {
        Set<String> key = getCacheKey(ruleId, hintMessage, hintTitle, fileLocationModel);
        return getCache(event).containsKey(key);
    }

    /**
     * Cache the status of the link between the provided {@link InlineHintModel} and the given {@link FileModel}.
     */
    static void cacheInlineHintModel(GraphRewrite event, InlineHintModel inlineHintModel)
    {
        getCache(event).put(getCacheKey(inlineHintModel), inlineHintModel.getElement());
    }

    private static Set<String> getCacheKey(InlineHintModel inlineHintModel)
    {
        /*
         * return getCacheKey(inlineHintModel.getRuleID(), inlineHintModel.getHint(), inlineHintModel.getTitle(), inlineHintModel.getEffort(),
         * inlineHintModel.getIssueDisplayMode(), inlineHintModel.getLineNumber(), inlineHintModel.getColumnNumber(), inlineHintModel.getLength(),
         * inlineHintModel.getFile().getFilePath(), inlineHintModel.getFileLocationReference().getSourceSnippit());
         */
        return getCacheKey(inlineHintModel.getRuleID(), inlineHintModel.getHint(), inlineHintModel.getTitle(),
                    inlineHintModel.getLineNumber(),
                    inlineHintModel.getColumnNumber(), inlineHintModel.getLength(),
                    inlineHintModel.getFile().getFilePath(), inlineHintModel.getFileLocationReference().getSourceSnippit());
    }

/*
    private static Set<String> getCacheKey(String ruleId, String hintMessage, String hintTitle, int effort,
                IssueDisplayMode issueDisplayMode, FileLocationModel fileLocationModel)
    {
        return getCacheKey(ruleId, hintMessage, hintTitle, effort, issueDisplayMode, fileLocationModel.getLineNumber(),
                    fileLocationModel.getColumnNumber(), fileLocationModel.getLength(), fileLocationModel.getFile().getFilePath(),
                    fileLocationModel.getSourceSnippit());
    }
*/

    private static Set<String> getCacheKey(String ruleId, String hintMessage, String hintTitle, FileLocationModel fileLocationModel)
    {
        return getCacheKey(ruleId, hintMessage, hintTitle, fileLocationModel.getLineNumber(),
                    fileLocationModel.getColumnNumber(), fileLocationModel.getLength(), fileLocationModel.getFile().getFilePath(),
                    fileLocationModel.getSourceSnippit());
    }

/*
    private static Set<String> getCacheKey(String ruleId, String hintMessage, String hintTitle, int effort,
                IssueDisplayMode issueDisplayMode, int lineNumber, int columnNumber, int length, String filePath, String sourceSnippit)
    {
        Set<String> properties = new HashSet<>(10);
        properties.add(ruleId);
        properties.add(Integer.toString(lineNumber));
        properties.add(Integer.toString(columnNumber));
        properties.add(Integer.toString(length));
        properties.add(Integer.toString(effort));
        properties.add(issueDisplayMode.name());
        properties.add(hintMessage);
        properties.add(hintTitle);
        properties.add(filePath);
        properties.add(sourceSnippit);
        return properties;
    }
*/

    private static Set<String> getCacheKey(String ruleId, String hintMessage, String hintTitle, int lineNumber, int columnNumber, int length,
                String filePath, String sourceSnippit)
    {
        Set<String> properties = new HashSet<>(8);
        properties.add(ruleId);
        properties.add(Integer.toString(lineNumber));
        properties.add(Integer.toString(columnNumber));
        properties.add(Integer.toString(length));
        properties.add(hintMessage);
        properties.add(hintTitle);
        properties.add(filePath);
        properties.add(sourceSnippit);
        return properties;
    }

    @Override
    public void beforeExecution(GraphRewrite event)
    {
        getCache(event).clear();
    }

    @Override
    public void afterExecution(GraphRewrite event)
    {
        getCache(event).clear();
    }
}
