package org.jboss.windup.rules.apps.javaee;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.HasApplications;
import org.jboss.windup.graph.model.HasProject;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.TagSetModel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.reporting.service.TagSetService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Adds the specified statistics and tag information regarding a technology that has been located in the analyzed application.
 *
 * If no count is specified, then a default of "1" is assumed.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TechnologyIdentified extends AbstractIterationOperation<WindupVertexFrame>
            implements TechnologyIdentifiedWithCount, TechnologyIdentifiedWithName
{
    public static final int DEFAULT_COUNT = 1;

    private static Logger LOG = Logging.get(TechnologyIdentified.class);

    private String technologyName;
    private Set<String> tags = new HashSet<>();
    private int count = DEFAULT_COUNT;

    /**
     * Creates the operation with the given technology name.
     */
    public TechnologyIdentified(String technologyName)
    {
        this.technologyName = technologyName;
    }

    /**
     * Creates the operation with the given input variable and technology name.
     *
     * This version also specifies the name of the iteration variable for cases in which there are multiple iteration variables being used. See also
     * {@link Iteration} and {@link AbstractIterationOperation}.
     */
    public TechnologyIdentified(String variableName, String technologyName)
    {
        super(variableName);
        this.technologyName = technologyName;
    }

    /**
     * Create the operation with the given technology name.
     */
    public static TechnologyIdentifiedWithName named(String technologyName)
    {
        return new TechnologyIdentified(technologyName);
    }

    /**
     * Sets the number of items that have been found by this operation.
     *
     * This is optional, and if left unspecified, it will default to {@link TechnologyIdentified#DEFAULT_COUNT}.
     */
    public TechnologyIdentifiedWithCount numberFound(int count)
    {
        this.count = count;
        return this;
    }

    /**
     * Specifies a tag to associate with this technology.
     */
    public TechnologyIdentified withTag(String tag)
    {
        this.tags.add(tag);
        return this;
    }

    /**
     * Contains the name of the technology identified by this rule.
     */
    public String getTechnologyName()
    {
        return technologyName;
    }

    /**
     * Contains the set of tags identified associated with this technology.
     */
    public Set<String> getTags()
    {
        return tags;
    }

    /**
     * Contains the count of items located by this operation (usually '1', and this is the default if nothing is specified).
     */
    public int getCount()
    {
        return count;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload)
    {
        Set<ProjectModel> projects = new HashSet<>();
        if (payload instanceof FileReferenceModel && ((FileReferenceModel) payload).getFile() != null)
            payload = ((FileReferenceModel) payload).getFile();

        if (payload instanceof ClassificationModel)
        {
            ((ClassificationModel) payload).getFileModels().forEach(fileModel -> {
                projects.add(fileModel.getProjectModel());
            });
        }
        else if (payload instanceof HasProject)
        {
            projects.add(((HasProject) payload).getProjectModel());
        }
        else if (payload instanceof HasApplications)
        {
            Iterable<ProjectModel> rootProjectModels = ((HasApplications) payload).getApplications();

            for (ProjectModel projectModel : rootProjectModels)
            {
                projects.add(projectModel);
            }
        }
        else if (payload instanceof TechnologyTagModel)
        {
            ((TechnologyTagModel) payload).getFileModels().forEach(fileModel -> projects.add(fileModel.getProjectModel()));
        }
        else
        {
            LOG.warning("Unrecognized payload for TechnologyIdentified. Payload must be instance of BelongsToProject.");
            LOG.warning(payload.toPrettyString());
            return;
        }

        for (ProjectModel project : projects)
        {
            TechnologyUsageStatisticsService service = new TechnologyUsageStatisticsService(event.getGraphContext());
            TechnologyUsageStatisticsModel stats = service.getOrCreate(project, this.technologyName);
            stats.setOccurrenceCount(stats.getOccurrenceCount() + this.count);

            // Update tags
            TagSetModel tagModel = stats.getTagModel();
            if (tagModel == null)
            {
                tagModel = new TagSetService(event.getGraphContext()).getOrCreate(event, this.tags);
                stats.setTagModel(tagModel);
            }
            if (!tagModel.getTags().equals(this.tags))
            {
                // Make sure to add any additionally specified tags
                Set<String> newSet = new HashSet<>(tagModel.getTags());
                newSet.addAll(this.tags);
                tagModel = new TagSetService(event.getGraphContext()).getOrCreate(event, this.tags);
                stats.setTagModel(tagModel);
            }
        }
    }
}
