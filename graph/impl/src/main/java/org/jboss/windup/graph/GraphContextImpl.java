package org.jboss.windup.graph;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.syncleus.ferma.ClassInitializer;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.MutationListener;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.EventStrategy;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.Mapping;
import org.janusgraph.diskstorage.berkeleyje.BerkeleyJEStoreManager;
import org.janusgraph.graphdb.database.StandardJanusGraph;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.furnace.util.Annotations;
import org.jboss.windup.graph.javahandler.JavaHandlerHandler;
import org.jboss.windup.graph.listeners.AfterGraphInitializationListener;
import org.jboss.windup.graph.listeners.BeforeGraphCloseListener;
import org.jboss.windup.graph.model.WindupEdgeFrame;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;

import com.sleepycat.je.LockMode;
import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.ReflectionCache;
import com.syncleus.ferma.Traversable;
import com.syncleus.ferma.WrappedFramedGraph;
import com.syncleus.ferma.framefactories.annotation.MethodHandler;

public class GraphContextImpl implements GraphContext {
    private static final Logger LOG = Logger.getLogger(GraphContextImpl.class.getName());

    private final Furnace furnace;
    private final GraphTypeManager graphTypeManager;
    private final Path graphDir;
    private final GraphApiCompositeClassLoaderProvider classLoaderProvider;
    /**
     * Used to save all the {@link BeforeGraphCloseListener}s that are also {@link AfterGraphInitializationListener}. This is due a need to call
     * {@link BeforeGraphCloseListener#beforeGraphClose()} on the same instance on which
     * {@link AfterGraphInitializationListener#afterGraphStarted(Map, GraphContext)} } was called
     */
    private final Map<String, BeforeGraphCloseListener> beforeGraphCloseListenerBuffer = new HashMap<>();
    private final List<GraphListener> graphListeners = new ArrayList<>();
    private Map<String, Object> configurationOptions;
    private JanusGraph graph;
    private WrappedFramedGraph<JanusGraph> framed;
    private Configuration conf;
    private static final GraphContextMutationListener mutationListener = new GraphContextMutationListener();

    public GraphContextImpl(Furnace furnace, GraphTypeManager typeManager,
                            GraphApiCompositeClassLoaderProvider classLoaderProvider, Path graphDir) {
        this.furnace = furnace;
        this.graphTypeManager = typeManager;
        this.classLoaderProvider = classLoaderProvider;
        this.graphDir = graphDir;
    }

    @Override
    public void registerGraphListener(GraphListener listener) {
        this.graphListeners.add(listener);
    }

    public GraphContextImpl create(boolean enableListeners) {
        FileUtils.deleteQuietly(graphDir.toFile());
        JanusGraph janusGraph = initializeJanusGraph(true, enableListeners);
        initializeJanusIndexes(janusGraph);
        createFramed(janusGraph);
        fireListeners();
        return this;
    }

    public GraphContextImpl load() {
        JanusGraph janusGraph = initializeJanusGraph(false, false);
        createFramed(janusGraph);
        fireListeners();
        return this;
    }

    private void fireListeners() {
        Imported<AfterGraphInitializationListener> afterInitializationListeners = furnace.getAddonRegistry().getServices(
                AfterGraphInitializationListener.class);
        Map<String, Object> confProps = new HashMap<>();
        Iterator<?> keyIter = conf.getKeys();
        while (keyIter.hasNext()) {
            String key = (String) keyIter.next();
            confProps.put(key, conf.getProperty(key));
        }

        if (!afterInitializationListeners.isUnsatisfied()) {
            for (AfterGraphInitializationListener listener : afterInitializationListeners) {
                listener.afterGraphStarted(confProps, this);
                if (listener instanceof BeforeGraphCloseListener) {
                    beforeGraphCloseListenerBuffer.put(listener.getClass().toString(), (BeforeGraphCloseListener) listener);
                }
            }
        }
    }

    private void createFramed(JanusGraph janusGraph) {
        this.graph = janusGraph;

        final ClassLoader compositeClassLoader = classLoaderProvider.getCompositeClassLoader();

        final ReflectionCache reflections = new ReflectionCache();

        Set<MethodHandler> handlers = new HashSet<>();
        handlers.add(new MapInPropertiesHandler());
        handlers.add(new MapInAdjacentPropertiesHandler());
        handlers.add(new MapInAdjacentVerticesHandler());
        handlers.add(new SetInPropertiesHandler());
        handlers.add(new JavaHandlerHandler());
        handlers.add(new WindupPropertyMethodHandler());
        handlers.add(new WindupAdjacencyMethodHandler());

        AnnotationFrameFactory frameFactory = new AnnotationFrameFactory(compositeClassLoader, reflections, handlers);

        /*
         * We override a couple of key methods here, just to insure that we always have access to the change events.
         *
         * Hopefully this will be fixed in a future version of Ferma.
         *
         * https://github.com/Syncleus/Ferma/issues/44
         */
        framed = new DelegatingFramedGraph<JanusGraph>(janusGraph, frameFactory, this.graphTypeManager) {
            @Override
            public <T> T addFramedVertex(final ClassInitializer<T> initializer, final Object... keyValues) {
                final Vertex vertex;
                final T framedVertex;
                if (keyValues != null) {
                    vertex = this.getBaseGraph().addVertex(keyValues);
                    framedVertex = frameNewElement(vertex, initializer);
                } else {
                    vertex = this.getBaseGraph().addVertex();
                    framedVertex = frameNewElement(vertex, initializer);
                }
                GraphContextImpl.this.mutationListener.vertexAdded(vertex);
                return framedVertex;
            }

            @Override
            public <T> T addFramedVertexExplicit(final ClassInitializer<T> initializer) {
                Vertex vertex = this.getBaseGraph().addVertex();
                final T framedVertex = frameNewElementExplicit(vertex, initializer);
                GraphContextImpl.this.mutationListener.vertexAdded(vertex);
                return framedVertex;
            }
        };
    }

    private List<Indexed> getIndexAnnotations(Method method) {
        List<Indexed> results = new ArrayList<>();
        Indexed index = method.getAnnotation(Indexed.class);
        if (index != null)
            results.add(index);

        Indexes indexes = method.getAnnotation(Indexes.class);
        if (indexes != null) {
            Collections.addAll(results, indexes.value());
        }

        return results;
    }

    private void initializeJanusIndexes(JanusGraph janusGraph) {
        Map<String, IndexData> defaultIndexKeys = new HashMap<>();
        Map<String, IndexData> searchIndexKeys = new HashMap<>();
        Map<String, IndexData> listIndexKeys = new HashMap<>();

        Set<Class<? extends WindupFrame<?>>> modelTypes = graphTypeManager.getRegisteredTypes();
        for (Class<? extends WindupFrame<?>> type : modelTypes) {
            for (Method method : type.getDeclaredMethods()) {
                List<Indexed> annotations = getIndexAnnotations(method);

                for (Indexed index : annotations) {
                    Property property = Annotations.getAnnotation(method, Property.class);
                    if (property != null) {
                        Class<?> dataType = index.dataType();
                        switch (index.value()) {
                            case DEFAULT:
                                defaultIndexKeys.put(property.value(), new IndexData(property.value(), index.name(), dataType));
                                break;

                            case SEARCH:
                                searchIndexKeys.put(property.value(), new IndexData(property.value(), index.name(), dataType));
                                break;

                            case LIST:
                                listIndexKeys.put(property.value(), new IndexData(property.value(), index.name(), dataType));
                                break;

                            default:
                                break;
                        }
                    }
                }
            }
        }

        /*
         * This is the root Model index that enables us to query on frame-type (by subclass type, etc.) Without this, every typed query would be slow.
         * Do not remove this unless something really paradigm-shifting has happened.
         */
        listIndexKeys.put(WindupVertexFrame.TYPE_PROP, new IndexData(WindupVertexFrame.TYPE_PROP, "", String.class));

        LOG.info("Detected and initialized [" + defaultIndexKeys.size() + "] default indexes: " + defaultIndexKeys);
        LOG.info("Detected and initialized [" + searchIndexKeys.size() + "] search indexes: " + searchIndexKeys);
        LOG.info("Detected and initialized [" + listIndexKeys.size() + "] list indexes: " + listIndexKeys);

        JanusGraphManagement janusGraphManagement = janusGraph.openManagement();
        for (Map.Entry<String, IndexData> entry : defaultIndexKeys.entrySet()) {
            String key = entry.getKey();
            IndexData indexData = entry.getValue();

            Class<?> dataType = indexData.type;

            PropertyKey propKey = getOrCreatePropertyKey(janusGraphManagement, key, dataType, Cardinality.SINGLE);
            janusGraphManagement.buildIndex(indexData.getIndexName(), Vertex.class).addKey(propKey).buildCompositeIndex();
        }

        for (Map.Entry<String, IndexData> entry : searchIndexKeys.entrySet()) {
            String key = entry.getKey();
            IndexData indexData = entry.getValue();
            Class<?> dataType = indexData.type;

            if (dataType == String.class) {
                PropertyKey propKey = getOrCreatePropertyKey(janusGraphManagement, key, String.class, Cardinality.SINGLE);
                janusGraphManagement.buildIndex(indexData.getIndexName(), Vertex.class).addKey(propKey, Mapping.STRING.asParameter())
                        .buildMixedIndex("search");
            } else {
                PropertyKey propKey = getOrCreatePropertyKey(janusGraphManagement, key, dataType, Cardinality.SINGLE);
                janusGraphManagement.buildIndex(indexData.getIndexName(), Vertex.class).addKey(propKey).buildMixedIndex("search");
            }
        }

        for (Map.Entry<String, IndexData> entry : listIndexKeys.entrySet()) {
            String key = entry.getKey();
            IndexData indexData = entry.getValue();
            Class<?> dataType = indexData.type;

            PropertyKey propKey = getOrCreatePropertyKey(janusGraphManagement, key, dataType, Cardinality.LIST);
            janusGraphManagement.buildIndex(indexData.getIndexName(), Vertex.class).addKey(propKey).buildCompositeIndex();
        }

        // Titan enforces items to be String, but there can be multiple items under one property name.
        String indexName = "edge-typevalue";
        PropertyKey propKey = getOrCreatePropertyKey(janusGraphManagement, WindupEdgeFrame.TYPE_PROP, String.class, Cardinality.LIST);
        janusGraphManagement.buildIndex(indexName, Edge.class).addKey(propKey).buildCompositeIndex();

        janusGraphManagement.commit();
    }

    private PropertyKey getOrCreatePropertyKey(JanusGraphManagement janusGraphManagement, String key, Class<?> dataType, Cardinality cardinality) {
        PropertyKey propertyKey = janusGraphManagement.getPropertyKey(key);
        if (propertyKey == null) {
            propertyKey = janusGraphManagement.makePropertyKey(key).dataType(dataType).cardinality(cardinality).make();
        }
        return propertyKey;
    }

    private JanusGraph initializeJanusGraph(boolean createMode, boolean enableListeners) {
        LOG.fine("Initializing graph.");

        Path lucene = graphDir.resolve("graphsearch");
        Path berkeley = graphDir.resolve("titangraph");

        // TODO: Externalize this.
        conf = new BaseConfiguration();

        // Sets a unique id in order to fix WINDUP-697. This causes Titan to not attempt to generate and ID,
        // as the Titan id generation code fails on machines with broken network configurations.
        conf.setProperty("graph.unique-instance-id", "windup_" + System.nanoTime() + "_" + RandomStringUtils.randomAlphabetic(6));
        conf.setProperty("storage.directory", berkeley.toAbsolutePath().toString());
        conf.setProperty("storage.backend", "berkeleyje");

        // Sets the berkeley cache to a relatively small value to reduce the memory footprint.
        // This is actually more important than performance on some of the smaller machines out there, and
        // the performance decrease seems to be minimal.
        conf.setProperty("storage.berkeleydb.cache-percentage", 1);

        // Set READ UNCOMMITTED to improve performance
        conf.setProperty("storage.berkeleydb.lock-mode", LockMode.READ_UNCOMMITTED);
        conf.setProperty("storage.berkeleydb.isolation-level", BerkeleyJEStoreManager.IsolationLevel.READ_UNCOMMITTED);

        // Increase storage write buffer since we basically do a large bulk load during the first phases.
        // See http://s3.thinkaurelius.com/docs/titan/current/bulk-loading.html
        conf.setProperty("storage.buffer-size", "4096");

        // Turn off transactions to improve performance
        conf.setProperty("storage.transactions", false);

        conf.setProperty("ids.block-size", 25000);
        // conf.setProperty("ids.flush", true);
        // conf.setProperty("", false);

        //
        // turn on a db-cache that persists across txn boundaries, but make it relatively small
        conf.setProperty("cache.db-cache", true);
        conf.setProperty("cache.db-cache-clean-wait", 0);
        conf.setProperty("cache.db-cache-size", .09);
        conf.setProperty("cache.db-cache-time", 0);

        conf.setProperty("index.search.backend", "lucene");
        conf.setProperty("index.search.directory", lucene.toAbsolutePath().toString());

        writeToPropertiesFile(conf, graphDir.resolve("TitanConfiguration.properties").toFile());
        JanusGraph janusGraph = JanusGraphFactory.open(conf);

        /*
         * We only need to setup the eventing system when initializing a graph, not when loading it later for
         * reporting.
         */
        if (enableListeners) {
            TraversalStrategies graphStrategies = TraversalStrategies.GlobalCache
                    .getStrategies(StandardJanusGraph.class)
                    .clone();

            // Remove any old listeners
            if (graphStrategies.getStrategy(EventStrategy.class) != null)
                graphStrategies.removeStrategies(EventStrategy.class);

            graphStrategies.addStrategies(EventStrategy.build().addListener(mutationListener).create());
            TraversalStrategies.GlobalCache.registerStrategies(StandardJanusGraph.class, graphStrategies);
            mutationListener.setGraph(this);
        }
        return janusGraph;
    }

    public Configuration getConfiguration() {
        return conf;
    }

    @Override
    public GraphTypeManager getGraphTypeManager() {
        return graphTypeManager;
    }

    @Override
    public void close() {
        try {
            Imported<BeforeGraphCloseListener> beforeCloseListeners = furnace.getAddonRegistry().getServices(BeforeGraphCloseListener.class);
            for (BeforeGraphCloseListener listener : beforeCloseListeners) {
                if (!beforeGraphCloseListenerBuffer.containsKey(listener.getClass().toString())) {
                    beforeGraphCloseListenerBuffer.put(listener.getClass().toString(), listener);
                }
            }
            for (BeforeGraphCloseListener listener : beforeGraphCloseListenerBuffer.values()) {
                listener.beforeGraphClose();
            }
            beforeGraphCloseListenerBuffer.clear();
        } catch (Exception e) {
            LOG.warning("Could not call before shutdown listeners during close due to: " + e.getMessage());
        }
        try {
            this.graph.close();
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Failed to close graph at: " + graphDir + " due to: " + t.getMessage(), t);
        }
    }

    @Override
    public void clear() {
        if (this.graph == null)
            return;
        if (this.graph.isOpen())
            close();

        try {
            JanusGraphFactory.drop(this.graph);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to delete graph due to: " + e.getMessage(), e);
        }
    }

    @Override
    public JanusGraph getGraph() {
        return graph;
    }

    @Override
    public WrappedFramedGraph<JanusGraph> getFramed() {
        return framed;
    }

    @Override
    public Traversable<?, ?> getQuery(Class<? extends WindupVertexFrame> kind) {
        return getFramed().traverse(g -> getFramed().getTypeResolver().hasType(g.V(), kind));
    }

    @Override
    public Path getGraphDirectory() {
        return graphDir;
    }

    @Override
    public Map<String, Object> getOptionMap() {
        if (this.configurationOptions == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(this.configurationOptions);
    }

    @Override
    public void setOptions(Map<String, Object> options) {
        this.configurationOptions = options;
    }

    @Override
    public String toString() {
        String graphHash = getGraph() == null ? "null" : "" + getGraph().hashCode();
        return "GraphContextImpl(" + hashCode() + "), Graph(" + graphHash + ") + DataDir(" + getGraphDirectory() + ")";
    }

    private void writeToPropertiesFile(Configuration conf, File file) {
        try {
            PropertiesConfiguration propConf = new PropertiesConfiguration(file);
            propConf.append(conf);
            propConf.save();
        } catch (ConfigurationException ex) {
            throw new RuntimeException("Failed writing Titan config to " + file.getAbsolutePath() + ": " + ex.getMessage(), ex);
        }
    }

    @Override
    public <T extends WindupVertexFrame> GraphService<T> service(Class<T> clazz) {
        return new GraphService<>(this, clazz);
    }

    @Override
    public <T extends WindupVertexFrame> T getUnique(Class<T> clazz) {
        return service(clazz).getUnique();
    }

    // --- Convenience delegations to new GraphService(this) --

    @Override
    public <T extends WindupVertexFrame> Iterable<T> findAll(Class<T> clazz) {
        return service(clazz).findAll();
    }

    @Override
    public <T extends WindupVertexFrame> T create(Class<T> clazz) {
        return service(clazz).create();
    }

    @Override
    public void commit() {
        getGraph().tx().commit();
    }

    private class IndexData {
        private final String propertyName;
        private final String indexName;
        private final Class<?> type;

        public IndexData(String propertyName, String indexName, Class<?> type) {
            this.propertyName = propertyName;
            this.indexName = indexName;
            this.type = type;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getIndexName() {
            return StringUtils.defaultIfBlank(indexName, propertyName);
        }

        public Class<?> getType() {
            return type;
        }

        @Override
        public String toString() {
            return String.format("IndexData{propertyName='%s', indexName='%s', type=%s}", propertyName, indexName, type);
        }
    }

    private static class GraphContextMutationListener implements MutationListener {
        private GraphContextImpl graphContext;

        /**
         * NOTE: This approach will break if we allow multiple execution threads in the same VM. We probably
         * shouldn't allow that anyway, though.
         */
        private void setGraph(GraphContextImpl graphContext) {
            this.graphContext = graphContext;
        }

        private GraphContextImpl getGraphContext() {
            return graphContext;
        }

        @Override
        public void vertexAdded(Vertex vertex) {
            GraphContextImpl graphContext = getGraphContext();
            if (graphContext == null || graphContext.graphListeners == null)
                return;
            getGraphContext().graphListeners.forEach(listener -> {
                listener.vertexAdded(vertex);
            });
        }

        @Override
        public void vertexPropertyChanged(Vertex vertex, VertexProperty oldValue, Object setValue,
                                          Object... vertexPropertyKeyValues) {
            GraphContextImpl graphContext = getGraphContext();
            if (graphContext == null || graphContext.graphListeners == null)
                return;
            getGraphContext().graphListeners.forEach(listener -> {
                listener.vertexPropertyChanged(vertex, oldValue, setValue, vertexPropertyKeyValues);
            });
        }

        @Override
        public void vertexRemoved(Vertex vertex) {

        }

        @Override
        public void vertexPropertyRemoved(VertexProperty vertexProperty) {

        }

        @Override
        public void edgeAdded(Edge edge) {

        }

        @Override
        public void edgeRemoved(Edge edge) {

        }

        @Override
        public void edgePropertyChanged(Edge element, org.apache.tinkerpop.gremlin.structure.Property oldValue, Object setValue) {

        }

        @Override
        public void edgePropertyRemoved(Edge element, org.apache.tinkerpop.gremlin.structure.Property property) {

        }

        @Override
        public void vertexPropertyPropertyChanged(VertexProperty element, org.apache.tinkerpop.gremlin.structure.Property oldValue, Object setValue) {

        }

        @Override
        public void vertexPropertyPropertyRemoved(VertexProperty element, org.apache.tinkerpop.gremlin.structure.Property property) {

        }
    }
}
