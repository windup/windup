package org.jboss.windup.graph;

import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This is used to provide a registry of {@link WindupFrame} subclasses for integration with Tinkerpop Frames.
 */
class GraphModelScanner {
    private static final Logger LOG = Logger.getLogger(GraphModelScanner.class.getName());
    private static final String NEWLINE = OperatingSystemUtils.getLineSeparator();

    public static List<Class<? extends WindupFrame<?>>> loadFrames(FurnaceClasspathScanner scanner) {
        List<Class<? extends WindupFrame<?>>> results = new ArrayList<>();

        // Scan for all classes form *Model.class.
        Predicate<String> modelClassFilter = new Predicate<String>() {
            // Package prefixes to skip.
            Set<String> skipPackages = new HashSet<>();

            {
                this.skipPackages.add("org.dom4j.");
                this.skipPackages.add("groovy.model.");
                this.skipPackages.add("com.sleepycat.persist.");
                this.skipPackages.add("org.apache.lucene.");
                this.skipPackages.add("org.openrdf.model.");
                this.skipPackages.add("org.apache.bcel.");
                this.skipPackages.add("org.eclipse.jdt.");
                this.skipPackages.add("org.eclipse.jface.");
                this.skipPackages.add("javax.");
                this.skipPackages.add("org.jboss.weld.");
                this.skipPackages.add("freemarker.");
            }

            @Override
            public boolean accept(String path) {
                if (!path.endsWith("Model.class"))
                    return false;

                final String clsName = PathUtil.classFilePathToClassname(path);
                for (String pkg : this.skipPackages)
                    if (clsName.startsWith(pkg))
                        return false;

                return true;
            }
        };

        LOG.info("Looking for *Model classes...");
        Iterable<Class<?>> classes = scanner.scanClasses(modelClassFilter);

        for (Class<?> clazz : classes) {
            if (WindupFrame.class.isAssignableFrom(clazz)) {
                LOG.fine("    Found: " + clazz);
                @SuppressWarnings("unchecked")
                Class<? extends WindupFrame<?>> windupVertexFrame = (Class<? extends WindupFrame<?>>) clazz;
                results.add(windupVertexFrame);
            } else {
                LOG.fine("    Not adding [" + clazz.getCanonicalName() + "] to GraphTypeRegistry");
            }
        }

        logLoadedModelTypes(results);
        return results;
    }

    private static void logLoadedModelTypes(List<Class<? extends WindupFrame<?>>> types) {
        List<Class<? extends WindupFrame<?>>> list = new ArrayList<>(types);
        Collections.sort(list, new Comparator<Class<? extends WindupFrame<?>>>() {
            @Override
            public int compare(Class<? extends WindupFrame<?>> left, Class<? extends WindupFrame<?>> right) {
                if (left == right)
                    return 0;
                if (left == null)
                    return 1;
                if (right == null)
                    return -1;
                return left.getCanonicalName().compareTo(right.getCanonicalName());
            }
        });
        StringBuilder result = new StringBuilder();
        for (Class<? extends WindupFrame<?>> frameType : list) {
            result.append("\t").append(frameType.getName()).append(NEWLINE);
        }

        LOG.info("Loaded [" + list.size() + "] WindupFrame sub-types [" + NEWLINE + result.toString() + "]");
    }
}
