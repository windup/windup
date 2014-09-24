package org.jboss.windup.graph;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.AbstractModule;
import com.tinkerpop.frames.modules.Module;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.FilenameUtil;
import org.jboss.windup.util.furnace.Filter;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;

@Singleton
public class GraphTypeRegistry
{
    private static final Logger LOG = Logger.getLogger(GraphTypeRegistry.class.getName());

    @Inject
    private FurnaceClasspathScanner scanner;

    @Inject
    private GraphTypeManager graphTypeManager;

    public void addTypeToElement(Class<? extends VertexFrame> kind, Element element)
    {
        graphTypeManager.addTypeToElement(kind, element);
    }

    @PostConstruct
    public void init()
    {
        // Scan for all classes form *Model.class.
        Filter<String> modelClassFilter = new Filter<String>()
        {
            // Package prefixes to skip.
            Set<String> skipPackages = new HashSet();
            {
                this.skipPackages.add("org.dom4j.");
                this.skipPackages.add("groovy.model.");
                this.skipPackages.add("com.sleepycat.persist.");
                this.skipPackages.add("org.apache.lucene.");
                this.skipPackages.add("org.openrdf.model.");
                this.skipPackages.add("org.apache.bcel.");
                this.skipPackages.add("org.eclipse.jdt.");
                this.skipPackages.add("javax.");
                this.skipPackages.add("org.jboss.weld.");
                this.skipPackages.add("freemarker.");
            }
            
            @Override
            public boolean accept(String path)
            {
                if(!path.endsWith("Model.class"))
                    return false;
                
                final String clsName = FilenameUtil.classFilePathToClassname(path);
                for(String pkg : this.skipPackages)
                    if(clsName.startsWith(pkg))
                        return false;
                
                return true;
            }
        };
        

        Iterable<Class<?>> classes = scanner.scanClasses(modelClassFilter);

        for (Class<?> clazz : classes)
        {
            if (WindupVertexFrame.class.isAssignableFrom(clazz))
            {
                @SuppressWarnings("unchecked")
                Class<? extends WindupVertexFrame> wvf = (Class<? extends WindupVertexFrame>) clazz;
                graphTypeManager.addTypeToRegistry(wvf);
            }
            else
            {
                LOG.log(Level.FINE, "Not adding [" + clazz.getCanonicalName()
                            + "] to GraphTypeRegistry");
            }
        }

        logLoadedModelTypes(graphTypeManager.getRegisteredTypes());
    }

    private void logLoadedModelTypes(Set<Class<? extends WindupVertexFrame>> types)
    {
        List<Class<? extends WindupVertexFrame>> list = new ArrayList<>(types);
        Collections.sort(list, new Comparator<Class<? extends WindupVertexFrame>>()
        {
            @Override
            public int compare(Class<? extends WindupVertexFrame> left, Class<? extends WindupVertexFrame> right)
            {
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
        for (int i = 0; i < list.size(); i++)
        {
            Class<?> type = list.get(i);
            result.append(type.getName());
            if ((i + 1) < list.size())
            {
                result.append(OperatingSystemUtils.getLineSeparator());
            }
        }
        LOG.info("Loaded [" + list.size() + "] WindupVertexFrame sub-types [" + OperatingSystemUtils.getLineSeparator()
                    + result.toString() + OperatingSystemUtils.getLineSeparator() + "]");
    }

    /**
     * Build TinkerPop Frames module - a collection of models.
     */
    public Module build()
    {
        return new AbstractModule()
        {
            @Override
            public void doConfigure(FramedGraphConfiguration config)
            {
                config.addTypeResolver(graphTypeManager);
                config.addFrameInitializer(graphTypeManager);
            }
        };
    }
}
