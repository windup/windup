package org.jboss.windup.reporting.freemarker;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Gets the number of effort points involved in migrating this application.
 *
 * <p> Called from a freemarker template as follows:
 *
 * <pre>getMigrationEffortPoints(
 *              projectModel: ProjectModel,
 *              recursive: Boolean,
 *              [includeTags: Set<String>],
 *              [excludeTags: Set<String>],
 *              [issueCategoryIDs: Set:<String>]
 *           ) : int
 *      </pre>
 *
 * <p> If recursive is true, the effort total includes child projects.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class GetEffortDetailsForProjectTraversalMethod implements WindupFreeMarkerMethod {
    public static final Logger LOG = Logger.getLogger(GetEffortDetailsForProjectTraversalMethod.class.getName());

    private static final String NAME = "getEffortDetailsForProjectTraversal";
    private ClassificationService classificationService;
    private InlineHintService inlineHintService;

    @Override
    public void setContext(GraphRewrite event) {
        this.classificationService = new ClassificationService(event.getGraphContext());
        this.inlineHintService = new InlineHintService(event.getGraphContext());
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes a " + ProjectModel.class.getSimpleName()
                + " as a parameter and returns Map<Integer, Integer> where the key is the effort level and the value is the number of incidents at that particular level of effort.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        // Process arguments
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() < 2) {
            throw new TemplateModelException(
                    "Error, method expects at least three arguments"
                            + " (projectModel: ProjectModel, recursive: Boolean, [includeTags: Set<String>], [excludeTags: Set<String>])");
        }
        StringModel projectModelTraversalArg = (StringModel) arguments.get(0);
        ProjectModelTraversal projectModelTraversal = (ProjectModelTraversal) projectModelTraversalArg.getWrappedObject();

        TemplateBooleanModel recursiveBooleanModel = (TemplateBooleanModel) arguments.get(1);
        boolean recursive = recursiveBooleanModel.getAsBoolean();

        Set<String> includeTags = Collections.emptySet();
        if (arguments.size() >= 3) {
            includeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(2));
        }

        Set<String> excludeTags = Collections.emptySet();
        if (arguments.size() >= 4) {
            excludeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(3));
        }

        Set<String> issueCategories = Collections.emptySet();
        if (arguments.size() >= 5) {
            issueCategories = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(4));
        }

        // Get values for classification and hints.
        Map<Integer, Integer> classificationEffortDetails =
                classificationService.getMigrationEffortByPoints(projectModelTraversal, includeTags, excludeTags, issueCategories, recursive, false);
        Map<Integer, Integer> hintEffortDetails =
                inlineHintService.getMigrationEffortByPoints(projectModelTraversal, includeTags, excludeTags, issueCategories, recursive, false);

        Map<Integer, Integer> results = sumMaps(classificationEffortDetails, hintEffortDetails);

        ExecutionStatistics.get().end(NAME);


        int points = sumPoints(results);
        LOG.fine(String.format("%s() FM function called:\n\t\t\tEFFORT: %3d = %s = C%s + H%s; %s, %srecur, tags: %s, excl: %s",
                NAME, points, results, classificationEffortDetails, hintEffortDetails,
                projectModelTraversal, recursive ? "" : "!", includeTags, excludeTags));

        return results;
    }


    private Map<Integer, Integer> sumMaps(Map<Integer, Integer> classificationEffortDetails, Map<Integer, Integer> hintEffortDetails) {
        Map<Integer, Integer> results = new HashMap<>(classificationEffortDetails.size() + hintEffortDetails.size());
        results.putAll(classificationEffortDetails);
        for (Map.Entry<Integer, Integer> entry : hintEffortDetails.entrySet()) {
            if (!results.containsKey(entry.getKey()))
                results.put(entry.getKey(), entry.getValue());
            else
                results.put(entry.getKey(), results.get(entry.getKey()) + entry.getValue());
        }
        return results;
    }


    private int sumPoints(Map<Integer, Integer> results) {
        int sum = 0;
        for (Map.Entry<Integer, Integer> entry : results.entrySet()) {
            sum += entry.getKey() * entry.getValue();
        }
        return sum;
    }
}
