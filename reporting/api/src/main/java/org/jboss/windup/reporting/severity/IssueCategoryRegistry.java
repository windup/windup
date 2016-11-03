package org.jboss.windup.reporting.severity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.Context;

import com.tinkerpop.blueprints.Vertex;

/**
 * Contains all {@link IssueCategory} objects that have been registered by Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class IssueCategoryRegistry
{
    public static final String MANDATORY = "mandatory";
    public static final String OPTIONAL = "optional";
    public static final String POTENTIAL = "potential-issue";
    public static final String DEFAULT = OPTIONAL;

    private Map<String, IssueCategory> issueCategories = new ConcurrentHashMap<>();

    /**
     * Gets an instance of the {@link IssueCategoryRegistry}.
     */
    public static IssueCategoryRegistry instance(Context context)
    {
        IssueCategoryRegistry registry = (IssueCategoryRegistry) context.get(IssueCategoryRegistry.class);
        if (registry == null)
        {
            registry = new IssueCategoryRegistry();
            context.put(IssueCategoryRegistry.class, registry);
        }
        return registry;
    }

    public IssueCategoryRegistry()
    {
        addDefaultsIfMissing();
    }

    /**
     * Attaches all registered {@link IssueCategory}s to the graph.
     */
    public void attachToGraph(GraphContext graphContext)
    {
        for (IssueCategory issueCategory : this.issueCategories.values())
        {
            IssueCategoryModel model = graphContext.create(IssueCategoryModel.class);
            model.setCategoryID(issueCategory.getCategoryID());
            model.setName(issueCategory.getName());
            model.setDescription(issueCategory.getDescription());
            model.setOrigin(issueCategory.getOrigin());
            model.setPriority(issueCategory.getPriority());
        }
    }

    /**
     * Loads the related graph vertex for the given {@link IssueCategory#getCategoryID()}.
     */
    public static IssueCategoryModel loadFromGraph(GraphContext graphContext, String issueCategoryID)
    {
        return loadFromGraph(graphContext.getFramed(), issueCategoryID);
    }

    /**
     * Loads the related graph vertex for the given {@link IssueCategory#getCategoryID()}.
     */
    public static IssueCategoryModel loadFromGraph(FramedGraph<EventGraph<TitanGraph>> framedGraph, String issueCategoryID)
    {
        Iterable<Vertex> vertices = framedGraph.query().has(WindupVertexFrame.TYPE_PROP, IssueCategoryModel.TYPE)
                .has(IssueCategoryModel.CATEGORY_ID, issueCategoryID).vertices();

        IssueCategoryModel result = null;
        for (Vertex vertex : vertices)
        {
            if (result != null)
                throw new DuplicateIssueCategoryException("Found more than one issue category for this id: " + issueCategoryID);

            result = framedGraph.frame(vertex, IssueCategoryModel.class);
        }
        return result;
    }

    /**
     * Loads the related graph vertex for the given {@link IssueCategory}.
     */
    public static IssueCategoryModel loadFromGraph(GraphContext graphContext, IssueCategory issueCategory)
    {
        return loadFromGraph(graphContext.getFramed(), issueCategory.getCategoryID());
    }

    /**
     * Adds a {@link IssueCategory} to the registry and throws a {@link DuplicateIssueCategoryException} if the item already exists.
     */
    public void addCategory(IssueCategory category) throws DuplicateIssueCategoryException
    {
        IssueCategory original = this.issueCategories.get(category.getCategoryID());

        // Just overwrite it if it was a placeholder
        if (!original.isPlaceholder())
        {
            StringBuilder message = new StringBuilder("Issue category (ID: ").append(category.getCategoryID())
                        .append(") is defined at the following locations:");
            message.append(OperatingSystemUtils.getLineSeparator());
            message.append("\t1: " + original.getOrigin());
            message.append("\t2: " + category.getOrigin());

            throw new DuplicateIssueCategoryException(message.toString());
        }

        this.issueCategories.put(category.getCategoryID(), category);
    }

    /**
     * Gets an {@link IssueCategory} by ID.
     */
    public IssueCategory getByID(String categoryID)
    {
        return this.issueCategories.get(categoryID);
    }

    /**
     * Returns a list ordered from the highest priority to the lowest.
     */
    public List<IssueCategory> getIssueCategories()
    {
        return this.issueCategories.values().stream()
                    .sorted((category1, category2) -> category1.getPriority() - category2.getPriority())
                    .collect(Collectors.toList());
    }

    /**
     * Make sure that we have some reasonable defaults available. These would typically be provided by the rulesets
     * in the real world.
     */
    private void addDefaultsIfMissing()
    {
        this.issueCategories.putIfAbsent(MANDATORY, new IssueCategory(MANDATORY, IssueCategoryRegistry.class.getCanonicalName(), "Mandatory", MANDATORY, 1000, true));
        this.issueCategories.putIfAbsent(OPTIONAL, new IssueCategory(OPTIONAL, IssueCategoryRegistry.class.getCanonicalName(), "Optional", OPTIONAL, 1000, true));
        this.issueCategories.putIfAbsent(POTENTIAL, new IssueCategory(POTENTIAL, IssueCategoryRegistry.class.getCanonicalName(), "Potential Issues", POTENTIAL, 1000, true));
    }
}
