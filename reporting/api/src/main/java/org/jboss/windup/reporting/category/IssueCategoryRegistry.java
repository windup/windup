package org.jboss.windup.reporting.category;

import com.syncleus.ferma.FramedGraph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.context.Context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Contains all {@link IssueCategory} objects that have been registered by Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class IssueCategoryRegistry {
    public static final String MANDATORY = "mandatory";
    public static final String OPTIONAL = "optional";
    public static final String POTENTIAL = "potential";
    public static final String CLOUD_MANDATORY = "cloud-mandatory";
    public static final String INFORMATION = "information";
    public static final String DEFAULT = OPTIONAL;

    private Map<String, IssueCategory> issueCategories = new ConcurrentHashMap<>();

    /**
     * Create a new {@link IssueCategoryRegistry}.
     */
    public IssueCategoryRegistry() {
        // Add some default values as placeholders. These will generally be further defined by the rulesets themselves.
        addDefaults();
    }

    /**
     * Gets an instance of the {@link IssueCategoryRegistry} from a {@link Context}. Note that the context variable
     * used might vary depending upon how this class is being used.
     * <p>
     * In some cases, the Context might be a part of the {@link RuleLoaderContext}, so that this registry can be used
     * during rule initialization. In other cases, it may be the {@link GraphRewrite} event's context.
     */
    public static IssueCategoryRegistry instance(Context context) {
        IssueCategoryRegistry registry = (IssueCategoryRegistry) context.get(IssueCategoryRegistry.class);
        if (registry == null) {
            registry = new IssueCategoryRegistry();
            context.put(IssueCategoryRegistry.class, registry);
        }
        return registry;
    }

    /**
     * Loads the related graph vertex for the given {@link IssueCategory#getCategoryID()}.
     */
    public static IssueCategoryModel loadFromGraph(GraphContext graphContext, String issueCategoryID) {
        return loadFromGraph(graphContext.getFramed(), issueCategoryID);
    }

    /**
     * Loads the related graph vertex for the given {@link IssueCategory#getCategoryID()}.
     */
    @SuppressWarnings("unchecked")
    public static IssueCategoryModel loadFromGraph(FramedGraph framedGraph, String issueCategoryID) {
        Iterable<Vertex> vertices = (Iterable<Vertex>) framedGraph.traverse(g -> framedGraph.getTypeResolver().hasType(g.V(), IssueCategoryModel.class))
                .traverse(g -> g.has(IssueCategoryModel.CATEGORY_ID, issueCategoryID)).getRawTraversal().toList();

        IssueCategoryModel result = null;
        for (Vertex vertex : vertices) {
            if (result != null)
                throw new DuplicateIssueCategoryException("Found more than one issue category for this id: " + issueCategoryID);

            result = framedGraph.frameElement(vertex, IssueCategoryModel.class);
        }
        return result;
    }

    /**
     * Loads the related graph vertex for the given {@link IssueCategory}.
     */
    public static IssueCategoryModel loadFromGraph(GraphContext graphContext, IssueCategory issueCategory) {
        return loadFromGraph(graphContext.getFramed(), issueCategory.getCategoryID());
    }

    /**
     * Attaches all registered {@link IssueCategory}s to the graph. This allows them to be more easily used
     * from rules.
     */
    public void attachToGraph(GraphContext graphContext) {
        for (IssueCategory issueCategory : this.issueCategories.values()) {
            IssueCategoryModel model = graphContext.create(IssueCategoryModel.class);
            model.setCategoryID(issueCategory.getCategoryID());
            model.setName(issueCategory.getName());
            model.setDescription(issueCategory.getDescription());
            model.setOrigin(issueCategory.getOrigin());
            model.setPriority(issueCategory.getPriority());
        }
    }

    /**
     * Adds a {@link IssueCategory} to the registry and throws a {@link DuplicateIssueCategoryException} if the item already exists.
     */
    public void addCategory(IssueCategory category) throws DuplicateIssueCategoryException {
        IssueCategory original = this.issueCategories.get(category.getCategoryID());

        // Just overwrite it if it was a placeholder
        if (original != null && !original.isPlaceholder()) {
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
    public IssueCategory getByID(String categoryID) {
        IssueCategory issueCategory = this.issueCategories.get(categoryID);
        if (issueCategory == null) {
            // We do not have this one yet, so store it as a placeholder. It will presumably be loaded later on.
            issueCategory = new IssueCategory(categoryID, "placeholder", categoryID, categoryID, 0, true);
            this.issueCategories.put(categoryID, issueCategory);
        }
        return issueCategory;
    }

    /**
     * Returns a list ordered from the highest priority to the lowest.
     */
    public List<IssueCategory> getIssueCategories() {
        return this.issueCategories.values().stream()
                .sorted((category1, category2) -> category1.getPriority() - category2.getPriority())
                .collect(Collectors.toList());
    }

    /**
     * Make sure that we have some reasonable defaults available. These would typically be provided by the rulesets
     * in the real world.
     */
    private void addDefaults() {
        this.issueCategories.putIfAbsent(MANDATORY, new IssueCategory(MANDATORY, IssueCategoryRegistry.class.getCanonicalName(), "Mandatory", MANDATORY, 1000, true));
        this.issueCategories.putIfAbsent(OPTIONAL, new IssueCategory(OPTIONAL, IssueCategoryRegistry.class.getCanonicalName(), "Optional", OPTIONAL, 1000, true));
        this.issueCategories.putIfAbsent(POTENTIAL, new IssueCategory(POTENTIAL, IssueCategoryRegistry.class.getCanonicalName(), "Potential Issues", POTENTIAL, 1000, true));
        this.issueCategories.putIfAbsent(CLOUD_MANDATORY, new IssueCategory(CLOUD_MANDATORY, IssueCategoryRegistry.class.getCanonicalName(), "Cloud Mandatory", CLOUD_MANDATORY, 1000, true));
        this.issueCategories.putIfAbsent(INFORMATION, new IssueCategory(INFORMATION, IssueCategoryRegistry.class.getCanonicalName(), "Information", INFORMATION, 1000, true));
    }
}
