package org.jboss.windup.graph.service;

import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import static org.jboss.windup.graph.model.WindupVertexFrame.TYPE_PROP;
import org.jboss.windup.graph.service.exception.NonUniqueResultException;

public class GraphService<T extends WindupVertexFrame> implements Service<T>
{
    @Inject
    private GraphContext context;

    private Class<T> type;

    protected GraphService(Class<T> type)
    {
        this.type = type;
    }

    public GraphService(GraphContext context, Class<T> type)
    {
        this(type);
        this.context = context;
    }

    public static WindupConfigurationModel getConfigurationModel(GraphContext context)
    {
        return new GraphService<>(context, WindupConfigurationModel.class).getUnique();
    }

    @Override
    public void commit()
    {
        this.context.getGraph().getBaseGraph().commit();
    }

    @Override
    public long count(Iterable<?> obj)
    {
        GremlinPipeline<Iterable<?>, Object> pipe = new GremlinPipeline<Iterable<?>, Object>();
        return pipe.start(obj).count();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T createInMemory()
    {
        Class<?>[] resolvedTypes = new Class<?>[] { VertexFrame.class, InMemoryVertexFrame.class, type };
        return (T) Proxy.newProxyInstance(this.type.getClassLoader(),
                    resolvedTypes, new FramedElementInMemory<>(context, this.type));
    }

    /**
     * Create a new instance of the given {@link WindupVertexFrame} type.
     * The ID is generated by the underlying graph database.
     */
    @Override
    public T create()
    {
        return context.getFramed().addVertex(null, this.type);
    }

    /**
     * Create a new instance of the given {@link WindupVertexFrame} type with given ID.
     */
    @Override
    public T create(Object id)
    {
        return context.getFramed().addVertex(id, this.type);
    }

    public void delete(T frame)
    {
        context.getFramed().removeVertex(frame.asVertex());
    }

    /**
     * Returns an iterable of all frames of given type.
     */
    @Override
    public Iterable<T> findAll()
    {
        FramedGraphQuery query = context.getFramed().query();
        query.has(TYPE_PROP, Text.CONTAINS, this.type.getAnnotation(TypeValue.class).value());
        return (Iterable<T>) query.vertices(this.type);
    }

    @Override
    public Iterable<T> findAllByProperties(String[] keys, String[] vals)
    {
        FramedGraphQuery fgq = context.getFramed().query().has(TYPE_PROP, Text.CONTAINS, getTypeValueForSearch());

        for (int i = 0, j = keys.length; i < j; i++)
        {
            String key = keys[i];
            String val = vals[i];

            fgq = fgq.has(key, val);
        }

        return fgq.vertices(this.type);
    }

    @Override
    public Iterable<T> findAllByProperty(String key, Object value)
    {
        return context.getFramed().getVertices(key, value, this.type);
    }

    @Override
    public Iterable<T> findAllByPropertyMatchingRegex(String key, String... regex)
    {
        if (regex.length == 0)
            return IterablesUtil.emptyIterable();

        final String regexFinal;
        if (regex.length == 1)
        {
            regexFinal = regex[0];
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            builder.append("\\b(");
            int i = 0;
            for (String value : regex)
            {
                if (i > 0)
                    builder.append("|");
                builder.append(value);
                i++;
            }
            builder.append(")\\b");
            regexFinal = builder.toString();
        }

        return context.getFramed().query().has(TYPE_PROP, Text.CONTAINS, getTypeValueForSearch())
                    .has(key, Text.REGEX, regexFinal).vertices(this.type);
    }

    
    /**
     * Returns the vertex with given ID framed into given interface.
     */
    @Override
    public T getById(Object id)
    {
        return context.getFramed().getVertex(id, this.type);
    }

    protected T frame(Vertex vertex)
    {
        return getGraphContext().getFramed().frame(vertex, this.getType());
    }

    @Override
    public Class<T> getType()
    {
        return this.type;
    }

    /**
     *  Returns the start of the graph query; further conditions may be appended.
     */
    protected TitanGraphQuery getTypedQuery()
    {
        return getGraphContext()
                    .getGraph().getBaseGraph().query().has("type", Text.CONTAINS, getTypeValueForSearch());
    }

    /**
     *  Returns what this' frame has in @TypeValue().
     */
    protected String getTypeValueForSearch()
    {
        TypeValue typeValue = this.type.getAnnotation(TypeValue.class);
        if (typeValue == null)
            throw new IllegalArgumentException("Must contain annotation 'TypeValue'");
        return typeValue.value();
    }

    @Override
    public T getUnique() throws NonUniqueResultException
    {
        Iterable<T> results = findAll();

        if (!results.iterator().hasNext())
        {
            return null;
        }

        Iterator<T> iter = results.iterator();
        T result = iter.next();

        if (iter.hasNext())
        {
            throw new NonUniqueResultException("Expected unique value, but returned non-unique.");
        }

        return result;
    }

    @Override
    public T getUniqueByProperty(String property, Object value) throws NonUniqueResultException
    {
        Iterable<T> results = findAllByProperty(property, value);

        if (!results.iterator().hasNext())
        {
            return null;
        }

        Iterator<T> iter = results.iterator();
        T result = iter.next();

        if (iter.hasNext())
        {
            throw new NonUniqueResultException("Expected unique value, but returned non-unique.");
        }

        return result;
    }

    protected GraphContext getGraphContext()
    {
        return context;
    }

    @Override
    public TitanTransaction newTransaction()
    {
        return context.getGraph().getBaseGraph().newTransaction();
    }

    public static List<WindupVertexFrame> toVertexFrames(GraphContext graphContext, Iterable<Vertex> vertices)
    {
        List<WindupVertexFrame> results = new ArrayList<>();
        for (Vertex v : vertices)
        {
            WindupVertexFrame frame = graphContext.getFramed().frame(v, WindupVertexFrame.class);
            results.add(frame);
        }
        return results;
    }

    /**
     * Adds the specified type to this frame, and returns a new object that implements this type.
     * 
     * @see GraphTypeManagerTest
     */
    public static <T extends WindupVertexFrame> T addTypeToModel(GraphContext graphContext, WindupVertexFrame frame,
                Class<T> type)
    {
        Vertex vertex = frame.asVertex();
        graphContext.getGraphTypeRegistry().addTypeToElement(type, vertex);
        return graphContext.getFramed().frame(vertex, type);
    }

    @Override
    public void remove(T model)
    {
        model.asVertex().remove();
    }

}
