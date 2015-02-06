package org.jboss.windup.rules.apps.java.scan.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.Importer;
import org.jboss.forge.roaster.spi.WildcardImportResolver;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;

/**
 * Provides a wildcard resolver for imports that attempts to search the graph for related types
 *
 */
public class WindupRoasterWildcardImportResolver implements WildcardImportResolver
{
    private static ThreadLocal<GraphContext> graphContextTL = new ThreadLocal<>();

    /**
     * Contains a map of class short names (eg, MyClass) to qualified names (eg, com.example.MyClass)
     */
    private final Map<String, String> classNameToFQCN = new HashMap<>();

    /**
     * Indicates that we have already attempted to query the graph for this particular shortname. The shortname will exist here even if no results
     * were found.
     */
    private final Set<String> classNameLookedUp = new HashSet<>();

    @Override
    public String resolve(JavaType<?> source, String type)
    {
        GraphContext graphContext = getGraphContext();
        if (graphContext == null)
        {
            return type;
        }

        // If the type contains a "." assume that it is fully qualified.
        // FIXME - This is a carryover from the original Windup code, and I don't think
        // that this assumption is valid.
        // Check if we have already looked this one up
        if (classNameLookedUp.contains(type))
        {
            // if yes, then just use the looked up name from the map
            String qualifiedName = classNameToFQCN.get(type);
            if (qualifiedName != null)
            {
                return qualifiedName;
            }
            else
            {
                // otherwise, just return the provided name (unchanged)
                return type;
            }
        }
        else
        {
            // if this name has not been resolved before, go ahead and resolve it from the graph (if possible)
            classNameLookedUp.add(type);

            // search every wildcard import for this name
            Importer<?> importer = (Importer<?>) source;
            for (Import importDeclaration : importer.getImports())
            {
                if (importDeclaration.isWildcard())
                {
                    String wildcardImport = importDeclaration.getQualifiedName();
                    String candidateQualifiedName = wildcardImport + "." + type;

                    JavaClassService javaClassService = new JavaClassService(graphContext);
                    Iterable<JavaClassModel> models = javaClassService.findAllByProperty(JavaClassModel.QUALIFIED_NAME,
                                candidateQualifiedName);
                    if (models.iterator().hasNext())
                    {
                        // we found it... put it in the map and return the result
                        classNameToFQCN.put(type, candidateQualifiedName);
                        return candidateQualifiedName;
                    }
                }
            }
            // nothing was found, so just return the original value
            return type;
        }
    }

    private GraphContext getGraphContext()
    {
        return graphContextTL.get();
    }

    public static void setGraphContext(GraphContext graphContext)
    {
        graphContextTL.set(graphContext);
    }
}
