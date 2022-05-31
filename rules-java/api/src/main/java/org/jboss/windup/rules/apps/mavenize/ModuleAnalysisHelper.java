package org.jboss.windup.rules.apps.mavenize;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.util.Logging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.logging.Logger;

/**
 * Contains methods for assistance in analyzing modules
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, ozizka at seznam.cz</a>
 */
public class ModuleAnalysisHelper {
    public static final String LAST_RESORT_DEFAULT_GROUP_ID = "com.mycompany.mavenized";
    private static final Logger LOG = Logging.get(ModuleAnalysisHelper.class);

    protected GraphContext graphContext;


    public ModuleAnalysisHelper(GraphContext context) {
        this.graphContext = context;
    }

    String deriveGroupId(ProjectModel projectModel) {
        if (projectModel instanceof MavenProjectModel) {
            MavenProjectModel mavenProject = (MavenProjectModel) projectModel;
            mavenProject.getGroupId();
        }

        // User can set this via --mavenizeGroupId.
        {
            String groupId = (String) graphContext.getOptionMap().get(MavenizeGroupIdOption.NAME);
            if (groupId != null) {
                if (groupId.matches(MavenizeGroupIdOption.REGEX_GROUP_ID))
                    return groupId;
                LOG.severe(MavenizeGroupIdOption.NAME + " doesn't match the groupId pattern, ignoring: " + groupId);
            }
        }

        // Try to take what the user put to --packages.
        {
            List<String> scanPackages = (List<String>) graphContext.getOptionMap().get(ScanPackagesOption.NAME);
            if (scanPackages != null && !scanPackages.isEmpty() && scanPackages.get(0).contains("."))
                return scanPackages.get(0);
        }

        // FIXME TODO - If this is unused, it should be removed.
        // Get shared prefix of all packages in this application. Can result to "".
        //String fromPackages = new ModuleAnalysisHelper(graphContext).deriveGroupIdFromPackages(projectModel);
        //if (fromPackages != null)
        //return fromPackages;

        return LAST_RESORT_DEFAULT_GROUP_ID;
    }

    /**
     * Counts the packages prefixes appearing in this project and if some of them make more than half of the total of existing packages, this prefix
     * is returned. Otherwise, returns null.
     * <p>
     * This is just a helper, it isn't something really hard-setting the package. It's something to use if the user didn't specify using
     * --mavenize.groupId, and the archive or project name is something insane, like few sencences paragraph (a description) or a number or such.
     */
    String deriveGroupIdFromPackages(ProjectModel projectModel) {
        Map<Object, Long> pkgsMap = new HashMap<>();
        Set<String> pkgs = new HashSet<>(1000);
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(graphContext.getGraph()).V(projectModel);


        pkgsMap = pipeline.out(ProjectModel.PROJECT_MODEL_TO_FILE)
                .has(WindupVertexFrame.TYPE_PROP, new P(new BiPredicate<String, String>() {
                    @Override
                    public boolean test(String o, String o2) {
                        return o.contains(o2);
                    }
                },
                        GraphTypeManager.getTypeValue(JavaClassFileModel.class)))
                .hasKey(JavaClassFileModel.PROPERTY_PACKAGE_NAME)
                .groupCount()
                .by(v -> upToThirdDot(graphContext, (Vertex) v)).toList().get(0);

        Map.Entry<Object, Long> biggest = null;
        for (Map.Entry<Object, Long> entry : pkgsMap.entrySet()) {
            if (biggest == null || biggest.getValue() < entry.getValue())
                biggest = entry;
        }
        // More than a half is of this package.
        if (biggest != null && biggest.getValue() > pkgsMap.size() / 2)
            return biggest.getKey().toString();

        return null;
    }

    public String upToThirdDot(final GraphContext context, final Vertex v) {
        JavaClassFileModel javaModel = context.getFramed().frameElement(v, JavaClassFileModel.class);
        String pkgName = javaModel.getPackageName();
        int upToThirdDot = StringUtils.ordinalIndexOf(pkgName, ".", 3);
        return StringUtils.substring(pkgName, 0, upToThirdDot);
    }
}
