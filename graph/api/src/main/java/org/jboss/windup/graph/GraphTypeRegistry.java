package org.jboss.windup.graph;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;
import org.jboss.windup.util.furnace.FurnaceScannerFilenameFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.AbstractModule;
import com.tinkerpop.frames.modules.Module;
import org.jboss.windup.log.jul.config.Logging;

@Singleton
public class GraphTypeRegistry
{
    private static final Logger LOG = LoggerFactory.getLogger(GraphTypeRegistry.class);

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


        LOG.info("Looking for *Model classes...");
        Iterable<Class<?>> classes = scanner.scanClasses(modelClassFilter);
        
        for (Class<?> clazz : classes)
        {
            // Add those extending WindupVertexFrame.
            //LOG.debug("Candidate: " + clazz);
            if (WindupVertexFrame.class.isAssignableFrom(clazz))
            {
                LOG.debug("    Found: " + clazz);
                @SuppressWarnings("unchecked")
                Class<? extends WindupVertexFrame> wvf = (Class<? extends WindupVertexFrame>) clazz;
                graphTypeManager.addTypeToRegistry(wvf);
            }
            else
            {
                LOG.debug("Skipping - not a WindupVertexFrame: " + clazz.getCanonicalName());
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
    
    static {
        Logging.init();
    }
}
