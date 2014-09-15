package org.jboss.windup.graph;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.forge.furnace.util.Iterators;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.ServiceLogger;
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

        ServiceLogger.logLoadedServices(LOG, WindupVertexFrame.class, Iterators.asList(classes));
        for (Class<?> clazz : classes)
        {
            // Add those extending WindupVertexFrame.
            if (WindupVertexFrame.class.isAssignableFrom(clazz))
            {
                @SuppressWarnings("unchecked")
                Class<? extends WindupVertexFrame> wvf = (Class<? extends WindupVertexFrame>) clazz;
                graphTypeManager.addTypeToRegistry(wvf);
            }
            else
            {
                LOG.log(Level.FINE, "Not adding to GraphTypeRegistry, not a subclass of WindupVertexFrame: "
                            + clazz.getCanonicalName());
            }
        }
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
