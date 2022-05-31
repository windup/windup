package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.Importer;
import org.jboss.forge.roaster.spi.WildcardImportResolver;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a wildcard resolver for imports that attempts to search the graph for related types
 */
public class WindupWildcardImportResolver implements WildcardImportResolver, org.jboss.windup.ast.java.WildcardImportResolver {
    private static GraphContext context;

    /**
     * Contains a map of class short names (eg, MyClass) to qualified names (eg, com.example.MyClass)
     */
    private final Map<String, String> classNameToFQCN = new ConcurrentHashMap<>();

    /**
     * Indicates that we have already attempted to query the graph for this particular shortname. The shortname will exist here even if no results
     * were found.
     */
    private final Set<String> classNameLookedUp = Collections.synchronizedSet(new HashSet<String>());

    @Override
    public String resolve(JavaType<?> source, String type) {
        GraphContext graphContext = getContext();
        if (graphContext == null) {
            return type;
        }

        Importer<?> importer = (Importer<?>) source;
        List<String> wildcardImports = new ArrayList<>();
        for (Import importDeclaration : importer.getImports()) {
            if (importDeclaration.isWildcard()) {
                wildcardImports.add(importDeclaration.getQualifiedName());
            }
        }
        return resolve(wildcardImports, type);
    }

    @Override
    public String resolve(List<String> wildcardImports, String type) {
        GraphContext graphContext = getContext();
        // If the type contains a "." assume that it is fully qualified.
        // FIXME - This is a carryover from the original Windup code, and I don't think
        // that this assumption is valid.
        // Check if we have already looked this one up
        if (classNameLookedUp.contains(type)) {
            // if yes, then just use the looked up name from the map
            String qualifiedName = classNameToFQCN.get(type);
            if (qualifiedName != null) {
                return qualifiedName;
            } else {
                // otherwise, just return the provided name (unchanged)
                return type;
            }
        } else {
            // if this name has not been resolved before, go ahead and resolve it from the graph (if possible)
            classNameLookedUp.add(type);

            // search every wildcard import for this name
            for (String wildcardImport : wildcardImports) {
                String candidateQualifiedName = wildcardImport + "." + type;

                JavaClassService javaClassService = new JavaClassService(graphContext);
                Iterable<JavaClassModel> models = javaClassService.findAllByProperty(JavaClassModel.QUALIFIED_NAME,
                        candidateQualifiedName);
                if (models.iterator().hasNext()) {
                    classNameToFQCN.put(type, candidateQualifiedName);
                    return candidateQualifiedName;
                }
            }
            // nothing was found, so just return the original value
            return type;
        }
    }

    @Override
    public String[] resolve(String wildcardImportPackageName) {
        JavaClassService javaClassService = new JavaClassService(getContext());
        Iterable<JavaClassModel> classModels = javaClassService.findByJavaPackage(wildcardImportPackageName);
        List<String> results = new ArrayList<>();
        for (JavaClassModel classModel : classModels) {
            results.add(classModel.getQualifiedName());
        }
        return results.toArray(new String[results.size()]);
    }

    private GraphContext getContext() {
        return WindupWildcardImportResolver.context;
    }

    public static void setContext(GraphContext context) {
        WindupWildcardImportResolver.context = context;
    }
}
