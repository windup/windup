package org.jboss.windup.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;
import org.jboss.windup.util.furnace.FurnaceScannerFilenameFilter;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.AbstractModule;
import com.tinkerpop.frames.modules.Module;

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
        FurnaceScannerFilenameFilter modelClassFilter = new FurnaceScannerFilenameFilter()
        {
            @Override
            public boolean accept(String name)
            {
                return name.endsWith("Model.class");
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
