package org.jboss.windup.reporting.freemarker;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import java.util.logging.Logger;

/**
 * Gets the number of effort points involved in migrating this application.
 *
 * <p> Called from a freemarker template as follows:
 *
 *      <pre>getMigrationEffortPoints(
 *              projectModel: ProjectModel,
 *              recursive: Boolean,
 *              storyPointsMode: SP_UNIQUE | SP_SHARED | SP_MIXED
 *              [includeTags: Set<String>],
 *              [excludeTags: Set<String>]
 *           ) : int
 *      </pre>
 *
 * <p> If recursive is true, the effort total includes child projects.
 *
 * <p> StoryPointsMode determines which story points are counted:
 *    <ul>
 *    <li><strong>SP_UNIQUE</strong> - only the story points of effort needed to migrate unique application code.</li>
 *    <li><strong>SP_SHARED</strong> - only the story points of modules of this app which are also used in other application.</li>
 *    <li><strong>SP_MIXED</strong> -  story points for both of the above, i.e. everything that is used in given application.</li>
 *    </ul>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetEffortDetailsForProjectTraversalMethod implements WindupFreeMarkerMethod
{
    public static Logger LOG = Logger.getLogger(GetEffortDetailsForProjectTraversalMethod.class.getName());

    private static final String NAME = "getEffortDetailsForProjectTraversal";
    private ClassificationService classificationService;
    private InlineHintService inlineHintService;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.classificationService = new ClassificationService(event.getGraphContext());
        this.inlineHintService = new InlineHintService(event.getGraphContext());
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a " + ProjectModel.class.getSimpleName()
                    + " as a parameter and returns Map<Integer, Integer> where the key is the effort level and the value is the number of incidents at that particular level of effort.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        // Process arguments
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() < 2)
        {
            throw new TemplateModelException(
                "Error, method expects at least three arguments"
                + " (projectModel: ProjectModel, recursive: Boolean, storyPointsMode: SP_UNIQUE | SP_SHARED | SP_MIXED, "
                + "[includeTags: Set<String>], [excludeTags: Set<String>])");
        }
        StringModel projectModelTraversalArg = (StringModel) arguments.get(0);
        ProjectModelTraversal projectModelTraversal = (ProjectModelTraversal) projectModelTraversalArg.getWrappedObject();

        TemplateBooleanModel recursiveBooleanModel = (TemplateBooleanModel) arguments.get(1);
        boolean recursive = recursiveBooleanModel.getAsBoolean();

        LOG.info("GetEffortDetailsForProjectTraversalMethod spMode: " + arguments.get(2).toString() + " " + arguments.get(2).getClass());///
        TemplateScalarModel storyPointsModeArg = (TemplateScalarModel) arguments.get(2);
        String storyPointsMode = storyPointsModeArg.getAsString();

        Set<String> includeTags = Collections.emptySet();
        if (arguments.size() >= 4)
        {
            includeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(3));
        }

        Set<String> excludeTags = Collections.emptySet();
        if (arguments.size() >= 5)
        {
            excludeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(4));
        }

        // Get values for classification and hints.
        Map<Integer, Integer> classificationEffortDetails =
                classificationService.getMigrationEffortByPoints(projectModelTraversal, includeTags, excludeTags, recursive, false);
        Map<Integer, Integer> hintEffortDetails =
                inlineHintService.getMigrationEffortByPoints(projectModelTraversal, includeTags, excludeTags, recursive, false);

        Map<Integer, Integer> results = new HashMap<>(classificationEffortDetails.size() + hintEffortDetails.size());
        results.putAll(classificationEffortDetails);
        for (Map.Entry<Integer, Integer> entry : hintEffortDetails.entrySet())
        {
            if (!results.containsKey(entry.getKey()))
                results.put(entry.getKey(), entry.getValue());
            else
                results.put(entry.getKey(), results.get(entry.getKey()) + entry.getValue());
        }

        ExecutionStatistics.get().end(NAME);
        return results;
    }
}
